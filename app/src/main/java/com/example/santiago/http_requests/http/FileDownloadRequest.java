package com.example.santiago.http_requests.http;

import android.content.Context;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileDownloadRequest extends BaseHttpRequest<File> {

	private static final int BUFFER_SIZE = 1024;

	private String fileUrl = null;
	private File outputFile = null;


	public FileDownloadRequest(Context context) {
		super(context);

	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	protected String getRequestUrl() {
		return fileUrl;
	}

	@Override
    protected String getHttpMethod() {
        return HttpMethod.GET;
    }

	@Override
	protected File parseResponse(Response response) throws HttpParseException {

		InputStream input = null;
		OutputStream output = null;

		try {
			input = response.body().byteStream();
			output = new FileOutputStream(outputFile);
	        byte[] buffer = new byte[BUFFER_SIZE];
	        int bytesRead = 0;
	        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
	            output.write(buffer, 0, bytesRead);
	        }

		} catch (IOException e) {

		    throw new HttpParseException(e);

        } finally{

			if(input!=null)
                try {input.close(); } catch (IOException e) {e.printStackTrace();}

			if (output!=null)
                try {output.close();} catch (IOException e) {e.printStackTrace();}

		}

		return outputFile;
	}

}
