module com.tugalsan.api.file.zip {
    requires zip4j;
    requires com.tugalsan.api.log;
    requires com.tugalsan.api.unsafe;
    requires com.tugalsan.api.file;
    requires com.tugalsan.api.executable;
    requires com.tugalsan.api.list;
    requires com.tugalsan.api.string;
    requires com.tugalsan.api.os;
    exports com.tugalsan.api.file.zip.server;
}
