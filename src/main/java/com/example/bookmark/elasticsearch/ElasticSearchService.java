/*
package com.example.bookmark.elasticsearch;

import com.example.bookmark.model.Bookmark;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ElasticSearchService {
    @Inject
    RestClient restClient;

    public void index(Bookmark bookmark) {
        try {
            System.out.println("ElasticSearchService.index()" + bookmark);
            Request request = new Request("PUT", "/bookmark/_doc/" + bookmark.getId());
            request.setJsonEntity(JsonObject.mapFrom(bookmark).toString());
            restClient.performRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateIndex(Bookmark old, Bookmark updated) {
        deleteIndex(old);
        index(updated);
    }

    public void deleteIndex(Bookmark bookmark) {
        try {
            Request request = new Request("DELETE", "/bookmark/_doc/" + bookmark.getId());
            request.setJsonEntity(JsonObject.mapFrom(bookmark).toString());
            restClient.performRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Bookmark> searchByTag(String tag) {
        try {
            return search("tags", "match", tag);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Bookmark> searchAll() {
        try {
            return search("tags", "match_all", null);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Bookmark> search(String term, String searchType, String match) throws IOException {
        Request request = new Request("GET", "/bookmark/_search");
        // construct a JSON query like {"query": {"match": {"<term>": "<match"}}
        JsonObject termJson = new JsonObject().put(term, match);
        JsonObject matchJson = new JsonObject().put(searchType, termJson);
        if("match".equals(searchType)) {
            JsonObject queryJson = new JsonObject().put("query", matchJson);
            request.setJsonEntity(queryJson.encode());
        }
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonObject json = new JsonObject(responseBody);
        JsonArray hits = json.getJsonObject("hits").getJsonArray("hits");
        List<Bookmark> results = new ArrayList<>(hits.size());
        for (int i = 0; i < hits.size(); i++) {
            JsonObject hit = hits.getJsonObject(i);
            Bookmark bookmark = hit.getJsonObject("_source").mapTo(Bookmark.class);
            results.add(bookmark);
        }
        return results;
    }
}
*/
