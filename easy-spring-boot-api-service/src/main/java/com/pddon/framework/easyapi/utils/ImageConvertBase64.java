package com.pddon.framework.easyapi.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.io.IOUtils;

/**
 * 目标处理的图片类别有：png，jpg，jpeg
 * <p>
 * 参考：
 * <ul>
 *   <li>[浅析data:image/png;base64的应用](https://www.cnblogs.com/ECJTUACM-873284962/p/9245474.html)</li>
 *   <li>[Base64](https://zh.wikipedia.org/wiki/Base64)</li>
 *   <li>[Data URLs](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs)
 * </ul>
 *
 * @author DoneSpeak
 * @date 2019/06/26
 */
public class ImageConvertBase64 {

    /**
     * 将图片文件转化为 byte 数组
     *
     * @param image 待处理图片文件
     * @return 图片文件转化为的byte数组
     */
    public static byte[] toBytes(File image) {
        try (FileInputStream input = new FileInputStream(image)) {
            // InputStream 的 available() 返回的值是该InputStream 在不被阻塞的情况下，一次可以读取到的数据长度。
            // byte[] imageBytes = new byte[input.available()];
            // input.read(imageBytes);
            return IOUtils.toByteArray(input);
        } catch (IOException e) {
            return null;
        }
    }

    public static String toBase64(byte[] bytes) {
        return bytesEncode2Base64(bytes);
    }

    /**
     * 将图片转化为 base64 的字符串
     *
     * @param image 待处理图片文件
     * @return 图片文件转化出来的 base64 字符串
     */
    public static String toBase64(File image) {
        return toBase64(image, false);
    }

    /**
     * 将图片转化为 base64 的字符串。如果<code>appendDataURLScheme</code>的值为true，则为图片的base64字符串拓展Data URL scheme。
     *
     * @param image               图片文件的路径
     * @param appendDataURLScheme 是否拓展 Data URL scheme 前缀
     * @return 图片文件转化为的base64字符串
     */
    public static String toBase64(File image, boolean appendDataURLScheme) {
        String imageBase64 = bytesEncode2Base64(toBytes(image));
        if (appendDataURLScheme) {
            imageBase64 = ImageDataURISchemeMapper.getScheme(image) + imageBase64;
        }
        return imageBase64;
    }

    private static String bytesEncode2Base64(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
    }

    private static byte[] base64Decode2Bytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * 将byte数组恢复为图片文件
     *
     * @param imageBytes 图片文件的 byte 数组
     * @param imagePath  恢复的图片文件的保存地址
     * @return 如果生成成功，则返回生成的文件路径，此时结果为参数的<code>imagePath</code>。否则返回 null
     */
    public static File toImage(byte[] imageBytes, File imagePath) {
        if (!imagePath.getParentFile().exists()) {
            imagePath.getParentFile().mkdirs();
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imagePath))) {
            bos.write(imageBytes);
            return imagePath;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将base64字符串恢复为图片文件
     *
     * @param imageBase64 图片文件的base64字符串
     * @param imagePath   恢复的图片文件的保存地址
     * @return 如果生成成功，则返回生成的文件路径，此时结果为参数的<code>imagePath</code>。。否则返回 null
     */
    public static File toImage(String imageBase64, File imagePath) {
        // base64 字符串中没有 ","
        int firstComma = imageBase64.indexOf(",");
        if (firstComma >= 0) {
            imageBase64 = imageBase64.substring(firstComma + 1);
        }
        return toImage(base64Decode2Bytes(imageBase64), imagePath);
    }

    /**
     * 保存 imageBase64 到指定文件中。如果<code>fileName</code>含有拓展名，则直接使用<code>fileName</code>的拓展名。
     * 否则，如果 <code>imageBase64</code> 为Data URLs，则更具前缀的来判断拓展名。如果无法判断拓展名，则使用“png”作为默认拓展名。
     *
     * @param imageBase64 图片的base64编码字符串
     * @param dir         保存图片的目录
     * @param fileName    图片的名称
     * @return 如果生成成功，则返回生成的文件路径。否则返回 null
     */
    public static File toImage(String imageBase64, File dir, String fileName) {
        File imagePath = null;
        if (fileName.indexOf(".") < 0) {
            String extension = ImageDataURISchemeMapper.getExtensionFromImageBase64(imageBase64, "png");
            imagePath = new File(dir, fileName + "." + extension);
        } else {
            imagePath = new File(dir, fileName);
        }
        return toImage(imageBase64, imagePath);
    }
}
