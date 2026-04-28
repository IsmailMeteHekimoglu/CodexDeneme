package com.futbolanaliz.servisler;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JsonServisi {
    private final ScriptEngine scriptEngine;

    public JsonServisi() {
        this.scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
    }

    public Object jsonOku(String json) {
        if (scriptEngine == null) {
            throw new IllegalStateException("JavaScript motoru bulunamadı. Bu proje Java 8 Nashorn JSON ayrıştırıcısını kullanır.");
        }

        try {
            String script = "Java.asJSONCompatible(" + json + ")";
            return scriptEngine.eval(script);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("JSON ayrıştırılamadı.", e);
        }
    }
}
