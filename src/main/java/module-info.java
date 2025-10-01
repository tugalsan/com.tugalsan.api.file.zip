module com.tugalsan.api.file.zip {
    requires zip4j;
    requires com.tugalsan.api.log;
    requires com.tugalsan.api.file;
    requires com.tugalsan.api.union;
    requires com.tugalsan.api.thread;
    requires com.tugalsan.api.function;
    requires com.tugalsan.api.list;
    requires com.tugalsan.api.string;
    requires com.tugalsan.api.os;
    exports com.tugalsan.api.file.zip.server;
}
