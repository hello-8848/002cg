package com.changgou.util;

import com.changgou.exception.ChanggouException;
import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * @Auther lxy
 * @Date
 */
//实现fastDFS文件管理
public class FastDFSClient {
    //加载tracker连接信息
    static {
        //获取tracker配置文件的位置
        String filePath = new ClassPathResource("fdfs_client.conf").getPath();
        //加载配置文件信息
        try {
            ClientGlobal.init(filePath);
        } catch (Exception e) {
            throw new ChanggouException("读取tracker配置文件异常");
        }
    }

    /**
     * 文件上传,返回文件上传的组和文件名字
     *
     * @param fastDFSFile :
     * @return : java.lang.String[]
     */
    public static String[] upload(FastDFSFile fastDFSFile) {
        //获取文件作者
        //创建存储附加信息数组
        NameValuePair[] meat_list = new NameValuePair[1];
        meat_list[0] = new NameValuePair("author",fastDFSFile.getAuthor());
        //定义文件上传后的返回值,存储文件所在组名和文件名
        String[] uploadResult = null;

        try {
            //创建trackerclient对象
            TrackerClient trackerClient = new TrackerClient();
            //trackerclient获取trackerserver信息
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建storageclient保存获得的服务信息
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //上传文件返回上传到的额组
            uploadResult = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meat_list);
        } catch (Exception e) {
            throw new ChanggouException("文件上传失败");
        }
        return uploadResult;
    }

    /**
     * 获取文件信息
     *
     * @param groupName      :
     * @param remoteFileName :
     * @return : org.csource.fastdfs.FileInfo
     */
    public static FileInfo getFile(String groupName, String remoteFileName) {
        try {
            //创建trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //通过trackerclient获取trackerserver信息
            TrackerServer trackerServer = trackerClient.getConnection();
            //保存信息到storageclient
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //使用storageclient读取文件信息,返回
            return storageClient.get_file_info(groupName, remoteFileName);
        } catch (Exception e) {
            throw new ChanggouException("读取文件信息失败");
        }
    }

    /**
     * 文件下载
     *
     * @param groupName      :
     * @param remoteFileName :
     * @return : java.io.InputStream
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) {
        try {
            //创建trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //通过trackerclient获得trackerserver信息
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建storageclient保存服务信息
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //使用storageclient下载文件
            byte[] bytes = storageClient.download_file(groupName, remoteFileName);
            //返回
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new ChanggouException("下载文件失败");
        }
    }


    /**
     * 删除文件
     *
     * @param groupName      :
     * @param remoteFileName :
     * @return : void
     */
    public static void deleteFile(String groupName, String remoteFileName) {
        try {
            //创建trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //使用tackerclient获得trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建storageclient保存服务信息
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //使用storageclient删除文件
            storageClient.delete_file(groupName, remoteFileName);
        } catch (Exception e) {
            throw new ChanggouException("删除文件失败");
        }
    }

    /**
     * 获取存储服务器中的组信息
     *
     * @param groupName :
     * @return : org.csource.fastdfs.StorageServer
     */
    public static StorageServer getStorages(String groupName) {
        try {
            //创建trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获得trackerserver信息
            TrackerServer trackerServer = trackerClient.getConnection();
            //使用trackerclient获得storageserver
            return trackerClient.getStoreStorage(trackerServer, groupName);
        } catch (Exception e) {
            throw new ChanggouException("获取storage组信息失败");
        }
    }

    /**
     * 根据组名和存储文件名获得存储服务信息
     *
     * @param groupName      :
     * @param remoteFileName :
     * @return : org.csource.fastdfs.ServerInfo[]
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) {
        try {
            //创建trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获得trackerserver信息
            TrackerServer trackerServer = trackerClient.getConnection();
            //使用trackerclient获得storageserver
            return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
        } catch (Exception e) {
            throw new ChanggouException("获取storage服务信息失败");
        }
    }

    /**
     * 获取tracker服务的地址
     *
     * @return : java.lang.String
     */
    public static String getTrackerUrl() {
        try {
            //创建trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获得trackerserver信息
            TrackerServer trackerServer = trackerClient.getConnection();
            //获的trackerserver的ip地址
            InetSocketAddress address = trackerServer.getInetSocketAddress();
            //获得trackerserver的端口号
            int port = ClientGlobal.getG_tracker_http_port();
            return "http://" + address + ":" + String.valueOf(port);
        } catch (IOException e) {
            throw new ChanggouException("获取tracker服务地址失败");
        }
    }

    /**
     * 获取TrackerServer
     *
     * @return : org.csource.fastdfs.TrackerServer
     */
    public static TrackerServer getTrackerServer() {
        //创建trackerclient
        try {
            TrackerClient trackerClient = new TrackerClient();
            return trackerClient.getConnection();
        } catch (IOException e) {
            throw new ChanggouException("获取trackerServer失败");
        }
    }

    /**
     * 获得StorageClient
     *
     * @return : org.csource.fastdfs.StorageClient
     */
    public static StorageClient getStorageClient() {
        //获得trackerServer
        return new StorageClient(getTrackerServer(), null);
    }
/**
 *测试方法
 * @param args :
 * @return : void
 */
    public static void main(String[] args) throws Exception {
        //测试获取文件信息
        FileInfo fileInfo = getFile("group1", "M00/00/00/wKjThF-55A2AIgstAAAf2wrXMLo421.jpg");
        System.out.println(fileInfo);
        //测试下载文件
        InputStream inputStream = downloadFile("group1", "M00/00/00/wKjThF-55A2AIgstAAAf2wrXMLo421.jpg");
        //创建输出流
        FileOutputStream fileOutputStream= new FileOutputStream(new File("C:\\Users\\luxueyi\\Desktop\\TeacherNote\\a.jpg"));
        //创建字节数组
        byte[] bytes = new byte[1024];
        //读取文件
        while (inputStream.read(bytes) != -1) {
            //输出文件
            fileOutputStream.write(bytes);
        }
        //释放资源
        fileOutputStream.close();
        inputStream.close();
        System.out.println("文件下载完成");
        //测试删除文件
        deleteFile("group1", "M00/00/00/wKjThF-55A2AIgstAAAf2wrXMLo421.jpg");
        System.out.println("文件删除完成");

    }

}
