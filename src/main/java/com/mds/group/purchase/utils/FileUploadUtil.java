/*
 * Copyright Ningbo Qishan Technology Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mds.group.purchase.utils;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.InputStream;

/**
 * The type File upload util.
 *
 * @author pavawi
 */
public class FileUploadUtil {


    private static final String UPLOAD_DIR = "/opt";
    private static final String PATH = "/resource/groupmall/";

    private static final String BASE_URL = "https://demo.bgniao.cn";

    public static String uploadFile(File file, String appmodelId) {
        String filename = appmodelId + "/" + FileTypeUtils.getPath(file.getName());
        File serverFile = new File(UPLOAD_DIR + PATH + filename);
        FileUtil.copy(file, serverFile, true);
        return BASE_URL + PATH + filename;
    }

    public static String uploadFile(InputStream is, String fileName, String appmodelId) {
        String filename = appmodelId + "/" + FileTypeUtils.getPath(fileName);
        File serverFile = new File(UPLOAD_DIR + PATH + filename);
        FileUtil.writeFromStream(is, serverFile);
        return BASE_URL + PATH + filename;
    }


}
