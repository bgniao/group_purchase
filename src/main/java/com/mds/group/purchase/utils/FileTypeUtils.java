package com.mds.group.purchase.utils;

import cn.hutool.core.util.IdUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * @description: 文件类型判断
 * @author: alan.fan
 * @create: 2018-07-24 14:44
 **/
public class FileTypeUtils {
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_FILE = "file";


    private static final String[][] ICON_TYPE_TABLE = {
            // {文件后缀名,文件所属类型}
            {"3gp", TYPE_VIDEO},
            {"aif", TYPE_AUDIO},
            {"aifc", TYPE_AUDIO},
            {"aiff", TYPE_AUDIO},
            {"als", TYPE_AUDIO},
            {"asf", TYPE_VIDEO},
            {"asx", TYPE_VIDEO},
            {"au", TYPE_AUDIO},
            {"avi", TYPE_VIDEO},
            {"awb", TYPE_AUDIO},
            {"bmp", TYPE_IMAGE},
            {"es", TYPE_AUDIO},
            {"esl", TYPE_AUDIO},
            {"fvi", TYPE_VIDEO},
            {"gif", TYPE_IMAGE},
            {"ico", TYPE_IMAGE},
            {"ief", TYPE_IMAGE},
            {"ifm", TYPE_IMAGE},
            {"ifs", TYPE_IMAGE},
            {"imy", TYPE_AUDIO},
            {"it", TYPE_AUDIO},
            {"itz", TYPE_AUDIO},
            {"j2k", TYPE_IMAGE},
            {"jpe", TYPE_IMAGE},
            {"jpeg", TYPE_IMAGE},
            {"jpg", TYPE_IMAGE},
            {"jpz", TYPE_IMAGE},
            {"lsf", TYPE_VIDEO},
            {"lsx", TYPE_VIDEO},
            {"m15", TYPE_AUDIO},
            {"m3u", TYPE_AUDIO},
            {"m3url", TYPE_AUDIO},
            {"ma1", TYPE_AUDIO},
            {"ma2", TYPE_AUDIO},
            {"ma3", TYPE_AUDIO},
            {"ma5", TYPE_AUDIO},
            {"mdz", TYPE_AUDIO},
            {"mid", TYPE_AUDIO},
            {"midi", TYPE_AUDIO},
            {"mil", TYPE_IMAGE},
            {"mio", TYPE_AUDIO},
            {"mng", TYPE_VIDEO},
            {"mod", TYPE_AUDIO},
            {"mov", TYPE_VIDEO},
            {"movie", TYPE_VIDEO},
            {"mp2", TYPE_AUDIO},
            {"mp3", TYPE_AUDIO},
            {"mp4", TYPE_VIDEO},
            {"mpe", TYPE_VIDEO},
            {"mpeg", TYPE_VIDEO},
            {"mpg", TYPE_VIDEO},
            {"mpg4", TYPE_VIDEO},
            {"mpga", TYPE_AUDIO},
            {"nbmp", TYPE_IMAGE},
            {"nsnd", TYPE_AUDIO},
            {"pac", TYPE_AUDIO},
            {"pae", TYPE_AUDIO},
            {"pbm", TYPE_IMAGE},
            {"pcx", TYPE_IMAGE},
            {"pda", TYPE_IMAGE},
            {"pgm", TYPE_IMAGE},
            {"pict", TYPE_IMAGE},
            {"png", TYPE_IMAGE},
            {"pnm", TYPE_IMAGE},
            {"pnz", TYPE_IMAGE},
            {"ppm", TYPE_IMAGE},
            {"pvx", TYPE_VIDEO},
            {"qcp", TYPE_AUDIO},
            {"qt", TYPE_VIDEO},
            {"qti", TYPE_IMAGE},
            {"qtif", TYPE_IMAGE},
            {"ra", TYPE_AUDIO},
            {"ram", TYPE_AUDIO},
            {"ras", TYPE_IMAGE},
            {"rf", TYPE_IMAGE},
            {"rgb", TYPE_IMAGE},
            {"rm", TYPE_AUDIO},
            {"rmf", TYPE_AUDIO},
            {"rmm", TYPE_AUDIO},
            {"rmvb", TYPE_AUDIO},
            {"rp", TYPE_IMAGE},
            {"rpm", TYPE_AUDIO},
            {"rv", TYPE_VIDEO},
            {"s3m", TYPE_AUDIO},
            {"s3z", TYPE_AUDIO},
            {"si6", TYPE_IMAGE},
            {"si7", TYPE_IMAGE},
            {"si9", TYPE_IMAGE},
            {"smd", TYPE_AUDIO},
            {"smz", TYPE_AUDIO},
            {"snd", TYPE_AUDIO},
            {"stm", TYPE_AUDIO},
            {"svf", TYPE_IMAGE},
            {"svg", TYPE_IMAGE},
            {"svh", TYPE_IMAGE},
            {"swf", TYPE_VIDEO},
            {"swfl", TYPE_VIDEO},
            {"tif", TYPE_IMAGE},
            {"tiff", TYPE_IMAGE},
            {"toy", TYPE_IMAGE},
            {"tsi", TYPE_AUDIO},
            {"ult", TYPE_AUDIO},
            {"vdo", TYPE_VIDEO},
            {"vib", TYPE_AUDIO},
            {"viv", TYPE_VIDEO},
            {"vivo", TYPE_VIDEO},
            {"vox", TYPE_AUDIO},
            {"vqe", TYPE_AUDIO},
            {"vqf", TYPE_AUDIO},
            {"vql", TYPE_AUDIO},
            {"wav", TYPE_VIDEO},
            {"wax", TYPE_AUDIO},
            {"wbmp", TYPE_IMAGE},
            {"wi", TYPE_IMAGE},
            {"wm", TYPE_VIDEO},
            {"wma", TYPE_AUDIO},
            {"wmv", TYPE_VIDEO},
            {"wmx", TYPE_VIDEO},
            {"wpng", TYPE_IMAGE},
            {"wv", TYPE_VIDEO},
            {"wvx", TYPE_VIDEO},
            {"xbm", TYPE_IMAGE},
            {"xm", TYPE_AUDIO},
            {"xmz", TYPE_AUDIO},
            {"xpm", TYPE_IMAGE},
            {"xwd", TYPE_IMAGE}};


    public static String getFileTypeName(String fileName) {
        String typeName = TYPE_FILE;
        String suffix = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0) {
            suffix = fileName;
        } else {
            suffix = FilenameUtils.getExtension(fileName).toLowerCase();
            if ("".equals(suffix)) {
                return typeName;
            }
        }
        for (String[] strings : ICON_TYPE_TABLE) {
            if (suffix.equals(strings[0])) {
                typeName = strings[1];
            }
        }
        return typeName;
    }

    public static String getFileTypeBySuffix(String suffix) {
        String typeName = TYPE_FILE;
        if ("".equals(suffix)) {
            return typeName;
        }
        for (String[] strings : ICON_TYPE_TABLE) {
            if (suffix.equals(strings[0])) {
                typeName = strings[1];
            }
        }
        return typeName;
    }

    /**
     * 文件路径
     *
     * @param fileFullName 文件
     * @return 返回上传路径
     */
    public static String getPath(String fileFullName) {

        StringBuffer keyPath = new StringBuffer(DateFormatUtils.format(new Date(), "yyyyMMdd"));
        //生成uuid
        String uuid = IdUtil.fastSimpleUUID();
        String extension = FilenameUtils.getExtension(fileFullName).toLowerCase();
        //文件路径
        String path = keyPath.append("/")
                .append(getFileTypeName(fileFullName))
                .append("/")
                .append(uuid)
                .append(".").append(extension).toString();
        return path;
    }


    public static void main(String[] args) {
        System.out.println(getFileTypeName("11.mp4"));
    }
}
