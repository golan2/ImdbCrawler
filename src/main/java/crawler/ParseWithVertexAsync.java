package crawler;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.JsonEvent;
import io.vertx.core.parsetools.JsonParser;

class ParseWithVertexAsync implements Handler<Buffer> {
    final JsonParser parser = JsonParser.newParser();



    //not working -- I stopped in the middls. it is too much work for when we already have the entire json in memory as Buffer



    @Override
    public void handle(Buffer buffer) {
        parser.objectEventMode();
        parser.handler(new AsyncJsonParser());
        parser.handle(buffer);
        parser.end();
    }

    private static class AsyncJsonParser implements Handler<JsonEvent> {
        enum State { begin, filmography, actor }
        AsyncJsonParser.State state = AsyncJsonParser.State.begin;

        AsyncJsonParser() {
            System.out.println("AsyncJsonParser CTOR");
        }

        @Override
        public void handle(JsonEvent event) {
            System.out.println(event.type() + " => fieldName=["+event.fieldName() +"]");
            switch (event.type()) {
                case START_ARRAY:
                    if (state== AsyncJsonParser.State.filmography && AsyncJsonParser.State.actor.toString().equals(event.fieldName())) {
                        state = AsyncJsonParser.State.actor;
                    }
                    break;
                case START_OBJECT:
                    if (state== AsyncJsonParser.State.begin && AsyncJsonParser.State.filmography.toString().equals(event.fieldName())) {
                        state = AsyncJsonParser.State.filmography;
                    }
                    else if (state== AsyncJsonParser.State.actor) {
                        System.out.println(event.toString());
                    }
                    break;
                case END_ARRAY:
                    break;
                case VALUE:
                    if (state== AsyncJsonParser.State.actor) {
                        System.out.println(event.toString());
                    }

                    break;
            }
        }
    }
}
