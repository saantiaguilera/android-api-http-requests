package com.example.santiago.http_requests.http;

import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * 
 * Clase para hacer pedidos de okHttp en un AsyncTask. Deben sobreescribirse
 *  los metodos getRequest() y onRequestCompleted(). getRequest() debe devolver 
 *  una referencia no nula de un objecto Request, de lo contrario onRequestCompleted()
 *  se llamará con un NullPointerException.
 * 
 * @author percha
 *
 */
public abstract class OkHttpAsyncRequest {
	
	private OkHttpRequestTask task = new OkHttpRequestTask();
	
	/**
	 * Ejecuta en un AsyncTask el pedido okHttp
	 */
	public void execute(){
		task.execute((Void) null);
	}
	
	/**
	 * Metodo a sobreescribir. Se debe devolver una referencia
	 *  no nula de un objecto Request, de lo contrario onRequestCompleted()
	 *  se llamará con un NullPointerException. Este metodo se llama dentro del AsyncTask
	 *  
	 * @return Request a ejecutarse en el AsyncTask
	 */
	abstract protected Request getRequest();
	
	/**
	 * Metodo a sobreescribir. Se devuelve el Response resultante del pedido y la 
	 * exception que se haya lanzado, o null si no se lanzó ninguna
	 * 
	 * @param httpResponse objeto resultante del pedido okHttp, nulo en caso de una excepcion
	 * @param exception excepcion que haya detenido el pedido, nulo en caso de no haberse detenido
	 */
	abstract protected void onRequestCompleted(Response httpResponse, Exception exception);
	
	private class OkHttpRequestTask  extends AsyncTask<Void, Void, OkHttpRequestTaskResponse > {
		
		@Override
		protected OkHttpRequestTaskResponse  doInBackground(Void... params) {
			
			OkHttpRequestTaskResponse result = new OkHttpRequestTaskResponse();
			
			try {
				
				Request request = getRequest();
				
				if(getRequest()!=null) {
					
					result.httpResponse = (new OkHttpClient()).newCall(request).execute();
					
				} else {
					
					result.httpResponse = null;
					result.exception = new NullPointerException("getRequest() must return a non null Request");
					
				}
				
			
			} catch (IOException exception) {
				exception.printStackTrace();
				
				result.httpResponse = null;
				result.exception = exception;
				
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(OkHttpRequestTaskResponse result) {
			
			onRequestCompleted(result.httpResponse, result.exception);
			
		}
		
	}
	
	private class OkHttpRequestTaskResponse {
		
		public Response httpResponse = null;
		public Exception exception = null;
		
	}
	
}

	
