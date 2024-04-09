package com.pddon.framework.easyapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileUtil {

    private static ResourceLoader resourceLoader;

    @Autowired
    @Lazy
    public void setResourceLoader(ResourceLoader resourceLoader) {
        FileUtil.resourceLoader = resourceLoader;
    }

    /**
     * 读取classpath路径下文件内容，以字符串形式返回文件内容
     * @param path
     * @return
     * @throws IOException
     */
    public static String loadClassPathFileAsString(String path) throws IOException {
        Resource resource = FileUtil.resourceLoader.getResource(String.format("classpath*:email/templates/%s", path));
        InputStream is = resource.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String data = null;
        StringBuffer buffer = new StringBuffer();
        while((data = br.readLine()) != null) {
            buffer.append(data);
        }

        br.close();
        isr.close();
        is.close();
        return buffer.toString();
    }

    public static boolean removeFile(String filePath, String fileName){
        try{
            File file = new File(filePath + File.separator + fileName);
            if(file.exists()){
                file.delete();
                log.info("删除文件【{}】成功!", file.getPath());
            }
            return true;
        }catch (Exception e){
            log.warn(IOUtils.getThrowableInfo(e));
            return false;
        }
    }

    public static String writeToPddonFile(String filePath, String fileName, String content){
        if(writeToFile(filePath, fileName, ".pddon", content)){
            return fileName + ".pddon";
        }
        return null;
    }
    public static String writeToJpgFile(String filePath, String fileName, String content){
        if(!content.startsWith("data:image")){
            int i = content.lastIndexOf('/');
            if(i > 0){
                return content.substring(i + 1);
            }
            return null;
        }
        try{
            File path = new File(filePath);
            File file = ImageConvertBase64.toImage(content, path, fileName);
            return file.getName();
        }catch (Exception e){
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }

    public static boolean writeToFile(String filePath, String fileName, String ext, String content) {
        try{
            Path path = Paths.get(filePath);
            if(!Files.isDirectory(path)){
                Files.createDirectories(path);
            }
            File file = new File(filePath, fileName + ext);
            log.info("saved file path: {}", file.getAbsolutePath());
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
            return true;
        }catch (IOException e){
            log.warn("保存文件失败");
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return false;
    }

    public static String readFromPddonFile(String filePath, String fileName){
        return readFromFile(filePath, fileName, ".pddon");
    }

    public static String readFromFile(String filePath, String fileName, String ext) {
        try{
            File file = new File(filePath, fileName + ext);
            if(!file.exists()){
                return null;
            }
            FileReader fileReader = new FileReader(file);
            List<String> lines = Files.readAllLines(file.toPath());
            return lines.stream().collect(Collectors.joining(""));
        }catch (IOException e){
            log.warn("读取文件内容失败");
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }

}
