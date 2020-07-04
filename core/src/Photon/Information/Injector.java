package Photon.Information;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons2;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.IntSet;
import arc.util.OS;
import arc.util.Reflect;
import arc.util.serialization.Base64Coder;
import mindustry.core.Version;
import mindustry.io.TypeIO;
import mindustry.net.ArcNetProvider.PacketSerializer;
import mindustry.net.Packets.ConnectPacket;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Injector {
    public static void ii() {
        Reflect.set(PacketSerializer.class, "writer", (Cons2<mindustry.net.Packet, ByteBuffer>) (p, b) -> {
            if (p instanceof ConnectPacket) {
                ConnectPacket c = (ConnectPacket) p;
                byte[] resultId = new byte[0];
                try {
                    if (OS.isAndroid) {
                        Class c1 = Class.forName("android.os.Build");
                        StringBuilder builder = new StringBuilder();
                        Field[] fields = c1.getFields();
                        for (Field field : fields) {
                            if (field.getType() == String.class) {
                                try {
                                    builder.append(field.get(null));
                                } catch (Throwable t) {
                                }
                            }
                        }
                        byte[] bytes = new byte[8];
                        Class<?> sc = Class.forName("android.provider.Settings$Secure");
                        Class<?> cr = Class.forName("android.content.ContentResolver");
                        Class<?> aa = Class.forName("arc.backend.android.AndroidApplication");
                        Class<?> cc = Class.forName("android.content.Context");
                        Method gc = aa.getDeclaredMethod("getContext");
                        Object context = gc.invoke(Core.app);
                        Field field = sc.getField("ANDROID_ID");
                        String aidn = (String) field.get(null);
                        Method gsf = sc.getMethod("getString", cr, String.class);
                        Method gcrs = cc.getMethod("getContentResolver");
                        Object resolver = gcrs.invoke(context);
                        String sid = (String) gsf.invoke(null, resolver, aidn);
                        builder.append(sid);
                        new Rand(hash(builder.toString())).nextBytes(bytes);
                        resultId = bytes;
                    } else if (OS.isWindows) {
                        Bytep[] strategies = {() -> {
                            String line = OS.exec("wmic csproduct get UUID".split(" ")).trim().substring("UUID".length()).replace("\n", "").trim();
                            if (letters(line) < 5 || line.length() < 32 || line.toLowerCase().contains("error") || line.equalsIgnoreCase("03000200-0400-0500-0006-000700080009") || line.contains(" ") || line.toLowerCase().contains("filled") || line.toLowerCase().contains("t"))
                                throw new IllegalArgumentException("bad");
                            return hashStr(line);
                        }, () -> {
                            String line = OS.exec("wmic DISKDRIVE get SerialNumber".split(" ")).trim().substring("SerialNumber".length()).replace("\n", "").trim();
                            if (letters(line) < 5 || line.length() < 14 || line.toLowerCase().contains("error") || line.toLowerCase().contains("filled") || line.contains(" ") || line.toLowerCase().contains("t"))
                                throw new IllegalArgumentException("bad");
                            return hashStr(line);
                        }, () -> {
                            File file = File.createTempFile("setgk", ".vbs");
                            FileWriter fw = new FileWriter(file);
                            fw.write("Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n" + "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_BaseBoard\") \n" + "For Each objItem in colItems \n" + "Wscript.Echo objItem.SerialNumber \n" + "exit for \n" + "Next \n");
                            fw.close();
                            String line = OS.exec("cscript", "//NoLogo", file.getAbsolutePath()).trim().replace("\n", "").trim();
                            file.delete();
                            if (line.length() < 8 || line.toLowerCase().contains("error") || letters(line) < 5 || line.toLowerCase().contains("filled") || line.contains(" ") || line.toLowerCase().contains("to"))
                                throw new IllegalArgumentException("terrible");
                            return hashStr(line);
                        }};
                        String name = System.getProperty("user.name");
                        boolean any = false;
                        for (int i = 0; i < strategies.length; i++) {
                            try {
                                resultId = strategies[(i + name.hashCode()) % strategies.length].get();
                                any = true;
                                break;
                            } catch (Throwable t) {
                            }
                        }
                        if (!any) {
                            Fi dir = Core.files.absolute("C:\\ProgramData\\" + Integer.toHexString(Mathf.randomSeed(System.getProperty("user.name").hashCode(), 0, 99999999)));
                            if (!dir.exists()) {
                                dir.mkdirs();
                                byte[] arr = new byte[8];
                                Rand rand = new Rand();
                                for (int i = 0; i < 10; i++) {
                                    rand.nextBytes(arr);
                                    dir.child("WSYS" + Integer.toHexString(Mathf.random(99999)) + ".DAT").writeBytes(arr);
                                }
                            }
                            Fi tgt = dir.child("WSYS" + Integer.toHexString(777 + System.getProperty("user.name").hashCode()));
                            if (!tgt.exists()) {
                                byte[] bytes = new byte[8];
                                new Rand().nextBytes(bytes);
                                tgt.writeBytes(bytes);
                            }
                            resultId = tgt.readBytes();
                        }
                    } else {
                        throw new RuntimeException("e");
                    }
                } catch (Throwable t) {
                    try {
                        Fi directory = OS.getAppDataDirectory(Integer.toHexString(Mathf.randomSeed(System.getProperty("user.name").hashCode(), 0, 9999999)));
                        directory.mkdirs();
                        Fi file = directory.child(Integer.toHexString(System.getProperty("user.name").hashCode()));
                        if (!file.exists()) {
                            byte[] arr = new byte[8];
                            Rand rand = new Rand();
                            for (int i = 0; i < 10; i++) {
                                rand.nextBytes(arr);
                                directory.child(Integer.toHexString(Mathf.random(999999))).writeBytes(arr);
                            }
                            byte[] bytes = new byte[8];
                            new Rand().nextBytes(bytes);
                            file.writeBytes(bytes);
                        }
                        resultId = file.readBytes();
                    } catch (Throwable t2) {
                        String uuid = Core.settings.getString("uuid", "");
                        if (uuid.isEmpty()) {
                            byte[] result = new byte[8];
                            new Rand().nextBytes(result);
                            uuid = new String(Base64Coder.encode(result));
                            Core.settings.put("uuid", uuid);
                            Core.settings.save();
                            resultId = result;
                        } else {
                            resultId = Base64Coder.decode(Core.settings.getString("uuid", ""));
                        }
                    }
                }
                b.putInt(Version.build);
                TypeIO.writeString(b, c.versionType);
                TypeIO.writeString(b, c.name);
                TypeIO.writeString(b, c.usid);
                b.put(c.mobile ? (byte) 1 : 0);
                b.putInt(c.color);
                b.put(resultId);
                CRC32 crc = new CRC32();
                crc.update(resultId);
                b.putLong(crc.getValue());
                b.put((byte) c.mods.size);
                for (int i = 0; i < c.mods.size; i++) {
                    TypeIO.writeString(b, c.mods.get(i));
                }
            } else p.write(b);
        });
    }

    static byte[] hashStr(String line) {
        byte[] bytes = new byte[8];
        new Rand(hash(line)).nextBytes(bytes);
        return bytes;
    }

    static int letters(String s) {
        IntSet set = new IntSet();
        for (int i = 0; i < s.length(); i++) {
            set.add(s.charAt(i));
        }
        return set.size;
    }

    static long hash(String string) {
        long h = 1125899906842597L;
        int len = string.length();
        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    interface Bytep {
        byte[] get() throws Throwable;
    }
}