package com.github.aesteve.vertx.web.dsl.body;

import com.github.aesteve.vertx.web.dsl.ResponseBuilder;
import com.github.aesteve.vertx.web.dsl.TestBase;
import com.github.aesteve.vertx.web.dsl.WebRouter;
import com.github.aesteve.vertx.web.dsl.io.BodyConverter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static com.github.aesteve.vertx.web.dsl.ResponseBuilder.found;

public class WithBodyTest extends TestBase {

    private final static String WITH_BODY_URL = "/tests/payload";
    private final static String WITH_BODY_MAP_URL = "/tests/payload/map";
    private final static String WITH_BODY_MAP_SEND_URL = "/tests/payload/map/send";
    private final static String WITH_BODY_CUSTOM_STATUS = "/tests/payload/map/custom";

    @Override
    protected WebRouter createRouter(Vertx vertx) {
        final WebRouter router = WebRouter.router(Vertx.vertx());
        router.converter("application/json", BodyConverter.JSON);
        router.post(WITH_BODY_URL)
                .withBody(MockObject.class)
                .fold(ResponseBuilder::ok);
        router.post(WITH_BODY_MAP_URL)
                .withBody(MockObject.class)
                .map(MockObject::getDate)
                .fold(ResponseBuilder::ok);
        router.post(WITH_BODY_MAP_SEND_URL)
                .withBody(MockObject.class)
                .map(MockObject::getDate)
                .fold(ResponseBuilder::ok);
        router.post(WITH_BODY_CUSTOM_STATUS)
                .withBody(MockObject.class)
                .map(MockObject::getDate)
                .fold(date -> found("unused", date));
        return router;
    }

    @Test
    public void bodyShouldBeReturned(TestContext ctx) throws Exception {
        final String sent = Json.mapper.writeValueAsString(new MockObject());
        testOk(WITH_BODY_URL, 200, sent, sent, ctx);
    }

    @Test
    public void bodyShouldBeMapped(TestContext ctx) throws Exception {
        final MockObject mock = new MockObject();
        final String expected = Json.mapper.writeValueAsString(mock.getDate());
        final String toSend = Json.mapper.writeValueAsString(mock);
        testOk(WITH_BODY_MAP_URL, 200, toSend, expected, ctx);
    }

    @Test
    public void bodyShouldBeMappedAndSent(TestContext ctx) throws Exception {
        final MockObject mock = new MockObject();
        final String expected = Json.mapper.writeValueAsString(mock.getDate());
        final String toSend = Json.mapper.writeValueAsString(mock);
        testOk(WITH_BODY_MAP_SEND_URL, 200, toSend, expected, ctx);
    }

    @Test
    public void customStatusReturned(TestContext ctx) throws Exception {
        final MockObject mock = new MockObject();
        final String expected = Json.mapper.writeValueAsString(mock.getDate());
        final String toSend = Json.mapper.writeValueAsString(mock);
        testOk(WITH_BODY_MAP_SEND_URL, 302, toSend, expected, ctx);
    }

    private void testOk(String url, int status, String toSend, String expected, TestContext ctx) {
        Async async = ctx.async();
        client().post(url, resp -> {
            ctx.assertEquals(200, resp.statusCode());
            resp.bodyHandler(buff -> {
                ctx.assertEquals(expected, buff.toString());
                async.complete();
            });
        }).putHeader(HttpHeaders.ACCEPT, "*/*").end(toSend);

    }
}
