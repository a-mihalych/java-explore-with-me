package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;

import java.util.List;

@Service
public class StatsClient extends BaseClient {

    @Autowired
   public StatsClient(@Value("${stats-server.uri}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addHit(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }

    public ResponseEntity<Object> hits(String start, String end, List<String> uris, Boolean unique) {
        StringBuilder stringBuilder = new StringBuilder();
        if (uris != null) {
            stringBuilder.append("uris=").append(String.join("&uris=", uris)).append("&");
        }
        String path = "/stats?start={start}&end={end}&" + stringBuilder + "unique={unique}";
        return get(path, start, end, unique);
    }
}
