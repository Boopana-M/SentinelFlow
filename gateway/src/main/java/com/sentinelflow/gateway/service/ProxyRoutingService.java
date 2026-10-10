package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewayRoutingProperties;
import com.sentinelflow.gateway.filter.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

@Service
public class ProxyRoutingService {

    private static final Logger log = LoggerFactory.getLogger(ProxyRoutingService.class);

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "host"
    );

    private final RestClient restClient;
    private final GatewayRoutingProperties routingProperties;

    public ProxyRoutingService(GatewayRoutingProperties routingProperties) {
        this.routingProperties = routingProperties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(routingProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(routingProperties.getReadTimeoutMs());

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public ResponseEntity<byte[]> forwardRequest(String targetBaseUrl,
                                                 String pathAndQuery,
                                                 HttpMethod method,
                                                 byte[] body,
                                                 HttpServletRequest incomingRequest) {
        String fullUrl = targetBaseUrl + pathAndQuery;
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        if (requestId == null) {
            requestId = incomingRequest.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        }

        log.info("Forwarding request method={} url={} requestId={}", method, fullUrl, requestId);

        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(method)
                    .uri(URI.create(fullUrl));

            // Copy incoming headers
            copyHeaders(incomingRequest, requestSpec);

            // Ensure X-Request-ID and X-Forwarded headers are present
            if (requestId != null) {
                requestSpec.header(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }
            requestSpec.header("X-Forwarded-For", incomingRequest.getRemoteAddr());
            requestSpec.header("X-Forwarded-Proto", incomingRequest.getScheme());

            if (body != null && body.length > 0) {
                requestSpec.body(body);
            }

            ResponseEntity<byte[]> response = requestSpec.retrieve()
                    .toEntity(byte[].class);

            HttpHeaders filteredResponseHeaders = filterResponseHeaders(response.getHeaders());
            if (requestId != null) {
                filteredResponseHeaders.set(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }

            return new ResponseEntity<>(response.getBody(), filteredResponseHeaders, response.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            log.warn("Upstream service returned status={} url={}", ex.getStatusCode(), fullUrl);
            HttpHeaders filteredResponseHeaders = filterResponseHeaders(ex.getResponseHeaders());
            if (requestId != null) {
                filteredResponseHeaders.set(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), filteredResponseHeaders, ex.getStatusCode());
        } catch (ResourceAccessException ex) {
            log.error("Failed to connect to upstream service url={}: {}", fullUrl, ex.getMessage());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (requestId != null) {
                headers.set(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }
            byte[] errorPayload = ("{\"error\":\"Bad Gateway\",\"message\":\"Upstream backend unreachable\",\"status\":502}")
                    .getBytes();
            return new ResponseEntity<>(errorPayload, headers, org.springframework.http.HttpStatus.BAD_GATEWAY);
        }
    }

    private void copyHeaders(HttpServletRequest request, RestClient.RequestBodySpec spec) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                    List<String> values = Collections.list(request.getHeaders(headerName));
                    spec.header(headerName, values.toArray(new String[0]));
                }
            }
        }
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders originalHeaders) {
        HttpHeaders headers = new HttpHeaders();
        if (originalHeaders != null) {
            originalHeaders.forEach((key, values) -> {
                if (!HOP_BY_HOP_HEADERS.contains(key.toLowerCase())) {
                    headers.put(key, values);
                }
            });
        }
        return headers;
    }
}
