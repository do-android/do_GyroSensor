package doext.implement;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import core.DoServiceContainer;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_GyroSensor_IMethod;

/**
 * 自定义扩展MM组件Model实现，继承do_GyroSensor_MAbstract抽象类，并实现do_GyroSensor_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_GyroSensor_Model extends DoSingletonModule implements do_GyroSensor_IMethod, SensorEventListener {
	private Context mContext;
	private Sensor gyroscopeSensor;
	private SensorManager sensorManager;

	public do_GyroSensor_Model() throws Exception {
		super();
		mContext = DoServiceContainer.getPageViewFactory().getAppContext();
		sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("getGyroData".equals(_methodName)) {
			getGyroData(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("start".equals(_methodName)) {
			start(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stop".equals(_methodName)) {
			stop(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName
	 *                    ,_invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	private double angleX;
	private double angleY;
	private double angleZ;
	// 创建常量，把纳秒转换为秒。
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			//从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
			if (timestamp != 0) {
				// 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
				final float dT = (event.timestamp - timestamp) * NS2S;
				// 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度 ， 将弧度转化为角度
				angleX = angleX + (float) Math.toDegrees(event.values[0] * dT);
				angleY = angleY + (float) Math.toDegrees(event.values[1] * dT);
				angleZ = angleZ + (float) Math.toDegrees(event.values[2] * dT);

				//顺时针 为负 ， 逆时针为正
				if (angleX > 180) {
					angleX = angleX - 180;
				}

				if (angleX < -180) {
					angleX = angleX + 180;
				}
				//顺时针 为负 ， 逆时针为正
				if (angleY > 180) {
					angleY = angleY - 180;
				}

				if (angleY < -180) {
					angleY = angleY + 180;
				}
				//顺时针 为负 ， 逆时针为正
				if (angleZ > 180) {
					angleZ = angleZ - 180;
				}

				if (angleZ < -180) {
					angleZ = angleZ + 180;
				}

				try {
					DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
					JSONObject _obj = new JSONObject();
					_obj.put("x", angleX);
					_obj.put("y", angleY);
					_obj.put("z", angleZ);
					_invokeResult.setResultNode(_obj);
					getEventCenter().fireEvent("change", _invokeResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			//将当前时间赋值给timestamp
			timestamp = event.timestamp;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void getGyroData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		try {
			JSONObject _result = new JSONObject();
			_result.put("x", angleX);
			_result.put("y", angleY);
			_result.put("z", angleZ);
			_invokeResult.setResultNode(_result);
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("do_GyroSensor_Model  \n\t", e);
		}
	}

	@Override
	public void start(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (gyroscopeSensor != null) {
			sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
		}
	}

	@Override
	public void stop(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		sensorManager.unregisterListener(this);
	}

	@Override
	public void dispose() {
		sensorManager.unregisterListener(this);
		super.dispose();
	}
}