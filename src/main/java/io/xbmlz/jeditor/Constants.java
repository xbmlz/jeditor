package io.xbmlz.jeditor;

import java.util.HashMap;
import java.util.Map;

public interface Constants {

    Map<String, String> LINE_SEPARATOR_MAP = new HashMap<String, String>() {{
        put("Windows (\\r\\n)", "\r\n");
        put("Unix (\\n)", "\n");
        put("Macintosh (\\r)", "\r");
    }};

    String[] ENCODINGS = {
            "UTF-8",
            "UTF-16",
            "UTF-16BE",
            "UTF-16LE",
            "US-ASCII",
            "ISO-8859-1",
    };

    Map<String, String> LANGUAGE_SYNTAX_MAP = new HashMap<String, String>() {{
        put("Plain Text", "text/plain");
        put("ActionScript", "text/actionscript");
        put("Assembler x86", "text/asm");
        put("Assembler 6502", "text/asm6502");
        put("BBCode", "text/bbcode");
        put("C", "text/c");
        put("Clojure", "text/clojure");
        put("C++", "text/cpp");
        put("C#", "text/cs");
        put("CSS", "text/css");
        put("D", "text/d");
        put("Dockerfile", "text/dockerfile");
        put("Dart", "text/dart");
        put("Pascal", "text/delphi");
        put("DTD", "text/dtd");
        put("Fortran", "text/fortran");
        put("Go", "text/golang");
        put("Groovy", "text/groovy");
        put("Handlebars", "text/handlebars");
        put("Hosts", "text/hosts");
        put("Htaccess", "text/htaccess");
        put("HTML", "text/html");
        put("INI", "text/ini");
        put("Java", "text/java");
        put("JavaScript", "text/javascript");
        put("JSON", "text/json");
        put("Jshintrc", "text/jshintrc");
        put("JSP", "text/jsp");
        put("Kotlin", "text/kotlin");
        put("LaTeX", "text/latex");
        put("Less", "text/less");
        put("Lisp", "text/lisp");
        put("Lua", "text/lua");
        put("Makefile", "text/makefile");
        put("Markdown", "text/markdown");
        put("MXML", "text/mxml");
        put("NSIS", "text/nsis");
        put("Perl", "text/perl");
        put("PHP", "text/php");
        put("PROTO", "text/proto");
        put("Properties", "text/properties");
        put("Python", "text/python");
        put("Ruby", "text/ruby");
        put("SAS", "text/sas");
        put("Scala", "text/scala");
        put("SQL", "text/sql");
        put("Tcl", "text/tcl");
        put("TypeScript", "text/typescript");
        put("UNIX", "text/unix");
        put("Visual Basic", "text/vb");
        put("Batch", "text/bat");
        put("XML", "text/xml");
        put("YAML", "text/yaml");
    }};
}
