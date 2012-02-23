package my.hmpoc.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class HmAndroidActivity extends Activity {
	
	private String imageLocation;
	private static final int IMAGE_INTENT_REQUEST_CODE = 10;
	ImageButton testImage;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("Coem on!!");
    	Log.d("debugtag", "In on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        testImage = (ImageButton)this.findViewById(R.id.launchCameraButton);
        testImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				invokeCamera();
			}
		});
    }
    
    private void invokeCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), UUID.randomUUID().toString() + ".png");
        assert !file.exists();
        imageLocation = file.getAbsolutePath();
        
        Uri outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, IMAGE_INTENT_REQUEST_CODE);
    }
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d("debugtag","On activity result");
    	Log.d("debugtag",requestCode+","+resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap thumbnail = (Bitmap) data.getExtras().get("data"); 
		if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_INTENT_REQUEST_CODE) {
			onPicTaken(thumbnail);
		}
	}
    
    private void onPicTaken(Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        //Bitmap bitmap = BitmapFactory.decodeFile(imageLocation, options);
        testImage.setImageBitmap(bitmap);
        
        new PostImageTask().execute(bitmap);
	}
    
    /**
     * To use AsyncTask you must subclass it. AsyncTask uses generics and varargs.The parameters are the following
     *  AsyncTask <TypeOfVarArgParams , ProgressValue , ResultValue> .
     *   TypeOfVarArgParams is passed into the doInBackground(), ProgressValueis used for progress information and 
     *   ResultValue must be returned from doInBackground() and is passed to onPostExecute() as parameter.
     * @author admin
     *
     */
    private class PostImageTask extends AsyncTask<Bitmap,Void,Boolean>{

		@Override
		protected Boolean doInBackground(Bitmap... bitmap) {
			
			Boolean success = true;
			Bitmap fileBitmap = bitmap[0];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			fileBitmap.compress(CompressFormat.JPEG, 100, bos);
			byte[] data = bos.toByteArray();
			
			ByteArrayBody bab = new ByteArrayBody(data,extractFileName(imageLocation));
			Log.d("debugtag", "image location --> " + imageLocation);
			 HttpClient httpClient = new DefaultHttpClient(); 
			 //FIXME this needs to be configurable
		        String postURL = "http://192.168.1.104:8080/yummie/api/v1/yummie/reviews/add";
		        HttpPost httpPost = new HttpPost(postURL);
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("uploaded", bab);
			try {
				reqEntity.addPart("photoCaption", new StringBody("sfsdfsdf"));
				httpPost.setEntity(reqEntity);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(null,e.getMessage());
				success = false;

			}
			
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
	    	postParams.add(new BasicNameValuePair("token", "token"));
			UrlEncodedFormEntity entity;
			try {
				entity = new UrlEncodedFormEntity(postParams, HTTP.UTF_8);
				httpPost.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(null,e.getMessage());
				success = false;
			}
            Log.i("infotag","This is where the post request is supposed to be called");           
            HttpResponse responsePOST;
			//try {
				//responsePOST = new MockHttpResponse(); //httpClient.execute(httpPost);
			//	 Log.i(null, "Post submitted" + responsePOST.getStatusLine());
//			} catch (ClientProtocolException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.e(null,e.getMessage());
//				success = false;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.e(null,e.getMessage());
//				success = false;
//			}
           
			
   
			return success;
		}

		private String extractFileName(String imageLocation) {
			// TODO Auto-generated method stub
			return "somefile.jpeg";
		}

		
    	
    }
}