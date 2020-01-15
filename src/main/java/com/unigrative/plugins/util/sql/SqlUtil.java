package com.unigrative.plugins.util.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SqlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlUtil.class);
    private final Map<String, String> storedSql = new ConcurrentHashMap();

    public static Object[] paramsListToString(List params){
        if (params == null) {
            return new Object[0];
        } else {
            Object[] strings = new String[params.size()];

            for(int i = 0; i < params.size(); ++i) {
                if (params.get(i) == null) {
                    strings[i] = "NULL";
                } else if (params.get(i) instanceof List) {
                    List<Object> list = (List)params.get(i);
                    if (list.isEmpty()) {
                        strings[i] = "NULL";
                    } else {
                        strings[i] = (String)list.stream().map(String::valueOf).collect(Collectors.joining(","));
                    }
                } else {
                    strings[i] = quote(params.get(i).toString());
                }
            }

            return strings;
        }
    }

    public static String paramsMapToWhereClause(HashMap params){

        boolean first = true;
        StringBuilder whereClause = new StringBuilder();

        if (params == null) {
            return "";
        } else {

            for (Object o : params.entrySet()) {
                Map.Entry e = (Map.Entry) o;
                String key = (String) e.getKey();
                Object comp = e.getValue();

                if(first) {
                    whereClause.append(" WHERE ").append(key.toString()).append(" LIKE ").append(quote(comp.toString()));
                    first = false;
                }
                else{
                    whereClause.append(" AND ").append(key.toString()).append(" LIKE ").append(quote(comp.toString()));
                }
            }
            return whereClause.toString();
        }
    }



    public static Object[] paramsToString(Object[] params) {
        if (params == null) {
            return new Object[0];
        } else {
            Object[] strings = new String[params.length];

            for(int i = 0; i < params.length; ++i) {
                if (params[i] == null) {
                    strings[i] = "NULL";
                } else if (params[i] instanceof List) {
                    List<Object> list = (List)params[i];
                    if (list.isEmpty()) {
                        strings[i] = "NULL";
                    } else {
                        strings[i] = (String)list.stream().map(String::valueOf).collect(Collectors.joining(","));
                    }
                } else {
                    strings[i] = quote(params[i].toString());
                }
            }

            return strings;
        }
    }

    public static String quote(Object o) {
        return "'" + o.toString().replaceAll("'", "''") + "'";
    }

    public String loadSql(String fileName, Object... params) {


        String sqlString = (String)this.storedSql.computeIfAbsent(fileName, (k) -> {
            try {
                InputStream stream = SqlUtil.class.getResourceAsStream(k);
                Throwable var2 = null;

                Object var7;
                try {
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                    Throwable var4 = null;

                    try {
                        Writer writer = new StringWriter();
                        Throwable var6 = null;

                        try {
                            while(reader.ready()) {
                                writer.write(reader.read());
                            }

                            var7 = writer.toString();
                        } catch (Throwable var54) {
                            var7 = var54;
                            var6 = var54;
                            throw var54;
                        } finally {
                            if (writer != null) {
                                if (var6 != null) {
                                    try {
                                        writer.close();
                                    } catch (Throwable var53) {
                                        var6.addSuppressed(var53);
                                    }
                                } else {
                                    writer.close();
                                }
                            }

                        }
                    } catch (Throwable var56) {
                        var4 = var56;
                        throw var56;
                    } finally {
                        if (reader != null) {
                            if (var4 != null) {
                                try {
                                    reader.close();
                                } catch (Throwable var52) {
                                    var4.addSuppressed(var52);
                                }
                            } else {
                                reader.close();
                            }
                        }

                    }
                } catch (Throwable var58) {
                    var2 = var58;
                    throw var58;
                } finally {
                    if (stream != null) {
                        if (var2 != null) {
                            try {
                                stream.close();
                            } catch (Throwable var51) {
                                var2.addSuppressed(var51);
                            }
                        } else {
                            stream.close();
                        }
                    }

                }

                return (String)var7;
            } catch (IOException var60) {
                LOGGER.error(var60.getMessage(), var60);
                LOGGER.error("Could not load file: {}", k);
                return "";
            }
        });
        return String.format(sqlString, params);
    }
}
