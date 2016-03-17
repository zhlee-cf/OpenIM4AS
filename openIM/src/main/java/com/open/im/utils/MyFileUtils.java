package com.open.im.utils;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.http.client.multipart.MultipartEntity;
import com.lidroid.xutils.http.client.multipart.content.FileBody;
import com.lidroid.xutils.http.client.multipart.content.StringBody;
import com.open.im.app.MyApp;
import com.open.im.bean.FileBean;

/**
 * 上传文件到服务器工具类，但是目前有问题，上传后返回的文件名都是以.jpg结尾
 * 
 * @author Administrator
 * 
 */
public class MyFileUtils {

	/**
	 * 直传发送文件 没有使用
	 * 
	 * @param ctx
	 * @param userID
	 * @param path
	 */
	public static void sendFile(Context ctx, String userID, String path) {
		AbstractXMPPConnection connection = MyApp.connection;
		FileTransferManager ftm = FileTransferManager.getInstanceFor(connection);
		OutgoingFileTransfer outgoingFileTransfer = ftm.createOutgoingFileTransfer(userID);
		File file = new File(path);
		try {
			outgoingFileTransfer.sendFile(file, "File");
			while (!outgoingFileTransfer.isDone()) {
				if (outgoingFileTransfer.getStatus().equals(FileTransfer.Status.in_progress)) {
					double progress = outgoingFileTransfer.getProgress();
					MyLog.showLog("进度::" + progress);
				}
			}
			if (outgoingFileTransfer.isDone()) {
				MyUtils.showToast((Activity) ctx, "发送完成");
			}
		} catch (SmackException e) {
			e.printStackTrace();
		}
	}

//	/**
//	 * 文件上传 开发环境  不再使用
//	 * 
//	 * @param filePath
//	 *            文件本机路径
//	 * @return 文件服务器保存路径
//	 */
//	public static String uploadFile(String filePath) {
//		String savePath = null;
//		File file = new File(filePath);
//		HttpPost httpPost = new HttpPost(MyConstance.UPLOAD_FILE_URL);
//		httpPost.addHeader(ExiuTokenHelpr.getHeaderKey(), ExiuTokenHelpr.getHeaderValue());
//		MultipartEntity entity = new MultipartEntity();
//		entity.addPart("File", new FileBody(file));
//		try {
//			entity.addPart("UploadPicType", new StringBody("0"));
//			httpPost.setEntity(entity);
//			HttpClient httpClient = new DefaultHttpClient();
//			// 发一个包一样的东西
//			HttpResponse httpResponse = httpClient.execute(httpPost);
//			if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
//				InputStream content = httpResponse.getEntity().getContent();
//				String parseStream = StringHelper.parseStream(content);
//				MyLog.showLog("完整的流::" + parseStream);
//				try {
//					JSONObject jsonObject = new JSONObject(parseStream);
//					int code = jsonObject.getInt("errorCode");
//					if (code != 0) {
//					} else {
//						String jResults = jsonObject.getString("result");
//						String result = jResults.substring(1, jResults.length() - 1);
//						// MyLog.showLog("result::" + result);
//						Gson gson = new Gson();
//						ImageBean bean = gson.fromJson(result, ImageBean.class);
//						MyLog.showLog("url::" + bean.getPicPath());
//						// http://121.42.153.9/ 开发环境前面拼这个
//						savePath = "http://121.42.153.9/" + bean.getPicPath();
//						// 生产 http://58.220.31.210/
//						// 开发 http://121.42.153.9/
//						// 测试 http://121.42.153.9/
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return savePath;
//	}

	/**
	 * 让保存的图片能在图库中能找到
	 * 
	 * @param path
	 *            图片的路径
	 */
	public static void scanFileToPhotoAlbum(Context ctx, String path) {
		// 媒体扫描服务
		MediaScannerConnection.scanFile(ctx, new String[] { path }, null, new OnScanCompletedListener() {

			@Override
			public void onScanCompleted(String path, Uri uri) {
				Log.i("lzh", "Finished scanning " + path);
			}
		});
	}

	/**
	 * httputils联网上传文件  没有使用
	 */
	public static void uploadFile(Context ctx, String filePath) {
		File file = new File(filePath);
		// 参数设置连接超时
		HttpUtils http = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("file", file);
		params.addBodyParameter("thumbnail", "1"); // 传1会返两个值，包含缩影图
		// params.
		http.send(HttpMethod.POST, MyConstance.UPDATE_URL, params, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				MyLog.showLog("上传成功:" + responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				MyLog.showLog("上传失败::" + error.getMessage());
			}
		});
	}

	/**
	 * httputils下载文件 没有使用
	 * 
	 * @param ctx
	 * @param url
	 */
	public static void downloadFile(final Context ctx, String path) {
		String fileName = path.substring(path.lastIndexOf(File.separator), path.length());
		String url = "http://im2.daimaqiao.net:8080/openstore/api/download.php";
		// 设置文件保存位置
		final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/exiu/receive" + fileName;
		HttpUtils http = new HttpUtils();
		RequestParams params = new RequestParams();

		params.addBodyParameter("fileid", path);

		http.download(HttpMethod.POST, url, filePath, params, true, true, new RequestCallBack<File>() {

			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				// pb.setMax((int) total);
				// pb.setProgress((int) current);
			}

			@Override
			public void onSuccess(ResponseInfo<File> responseInfo) {
				MyLog.showLog("下载成功:" + responseInfo.result.length());
				// scanFileToPhotoAlbum(ctx, filePath);

			}

			@Override
			public void onFailure(HttpException error, String msg) {
				MyLog.showLog("下载失败::" + error.getMessage());
			}
		});
	}
	/**
	 * 上传文件 正在使用
	 * @param srcPath
	 * @return
	 */
	public static FileBean upLoadByHttpClient(String srcPath) {
		try {
			
			MyLog.showLog("path::" + srcPath);
			
			FileBean bean = new FileBean();
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(MyConstance.UPDATE_URL);
			File file = new File(srcPath);
			MultipartEntity entity = new MultipartEntity();
			FileBody fileBody = new FileBody(file);
			entity.addPart("file", fileBody);
			entity.addPart("thumbnail", new StringBody("1"));
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				String result = EntityUtils.toString(resEntity);
				MyLog.showLog("result::" + result);
				bean = (FileBean) bean.fromJson(result);
				String error = bean.getError();
				if (TextUtils.isEmpty(error)) {
					MyLog.showLog("上传成功:" + bean);
				} else {
					MyLog.showLog("上传失败");
					return null;
				}
			}
			if (resEntity != null) {
				resEntity.consumeContent();
			}
			httpclient.getConnectionManager().shutdown();
			return bean;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}