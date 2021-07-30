package demo;

import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class weChatImgRevert {

    public static void main(String[] args) {
        String path = "G:\\微信文件\\WeChat Files\\wxid_xeg3p7zmr1rf22\\FileStorage\\Image";
        String targetPath = "D:\\demo2\\";
        int xor = 0xF9F9;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 5, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        threadPoolExecutor.execute(() -> {
            convert(path, targetPath, xor);
        });
    }










    /**
     * @param path       图片地址
     * @param targetPath 转换后目录
     */
    private static void convert(String path, String targetPath, int xor) {

        File[] fileAll = new File(path).listFiles();
        if (fileAll == null) {
            return;
        }
        for (File file2 : fileAll) {
            File[] file = new File(file2.getPath()).listFiles();
            int size = file.length;
            System.out.println("总共" + size + "个文件");
            AtomicReference<Integer> integer = new AtomicReference<>(0);
            Arrays.stream(file).parallel().forEach(file1 -> {
                String[] split = file1.getParent().split("\\\\");
                File file3 = new File(targetPath + "\\" + split[split.length - 1]);
                file3.mkdir();
                try (InputStream reader = new FileInputStream(file1);
                     OutputStream writer =
                             new FileOutputStream(file3.getPath()+"\\" + file1.getName().split("\\.")[0] + ".jpg")) {
                    byte[] bytes = new byte[1024 * 10];
                    int b;
                    while ((b = reader.read(bytes)) != -1) {//这里的in.read(bytes);就是把输入流中的东西，写入到内存中（bytes）。
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = (byte) (int) (bytes[i] ^ xor);
                            if (i == (b - 1)) {
                                break;
                            }
                        }
                        writer.write(bytes, 0, b);
                        writer.flush();
                    }
                    integer.set(integer.get() + 1);
                    System.out.println(file1.getName() + "(大小:" + ((double) file1.length() / 1000) + "kb),进度：" + integer.get() +
                            "/" + size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }


        System.out.println("解析完毕！");
    }

    /**
     * 获取异或值，不一定准确，当解析不出来的时候，换一张图片的异或值来解析
     *
     * @param PhotoPath
     * @return
     */
    private static int getXor(String PhotoPath) {
        File file = new File(PhotoPath);
        try (InputStream reader = new FileInputStream(file)) {
            int[] xors = new int[4];
            xors[0] = reader.read() & 0xFF ^ 0xFF;
            xors[1] = reader.read() & 0xFF ^ 0xD8;
            reader.skip(file.length() - 1);
            xors[2] = reader.read() & 0xFF ^ 0xFF;
            xors[3] = reader.read() & 0xFF ^ 0xD9;
            Map<Integer, Integer> map = new HashMap<>();
            for (int xor : xors) {
                if (map.containsKey(xor)) {
                    map.put(xor, map.get(xor) + 1);
                } else {
                    map.put(xor, 1);
                }
            }
            return map.values().stream().max(Integer::compareTo).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
