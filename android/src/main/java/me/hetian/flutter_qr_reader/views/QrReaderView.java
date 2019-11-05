package me.hetian.flutter_qr_reader.views;

import android.app.ActionBar;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

public class QrReaderView implements PlatformView, MethodChannel.MethodCallHandler {

    private final MethodChannel mMethodChannel;
    private final Context mContext;
    private Map<String, Object> mParams;
    private PluginRegistry.Registrar mRegistrar;
    private DecoratedBarcodeView decoratedBarcodeView;

    public static String EXTRA_FOCUS_INTERVAL = "extra_focus_interval";
    public static String EXTRA_TORCH_ENABLED = "extra_torch_enabled";

    public QrReaderView(Context context, PluginRegistry.Registrar registrar, int id, Map<String, Object> params){
        this.mContext = context;
        this.mParams = params;
        this.mRegistrar = registrar;

        // 创建视图
        int width = (int) mParams.get("width");
        int height = (int) mParams.get("height");
        decoratedBarcodeView = new DecoratedBarcodeView(mContext);
//        _view = new QRCodeReaderView(mContext);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(width, height);
        decoratedBarcodeView.setLayoutParams(layoutParams);

        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.CODE_39);

        decoratedBarcodeView.getBarcodeView().setDecoderFactory( new DefaultDecoderFactory(formats));
        decoratedBarcodeView.decodeContinuous(new ScannCallback());
        decoratedBarcodeView.setStatusText("");
//        _view.setLayoutParams(layoutParams);
//        _view.setOnQRCodeReadListener(this);
//        _view.setQRDecodingEnabled(true);
//        _view.forceAutoFocus();
//        int interval = mParams.containsKey(EXTRA_FOCUS_INTERVAL) ? (int) mParams.get(EXTRA_FOCUS_INTERVAL) : 2000;
//        _view.setAutofocusInterval(interval);
//        _view.setTorchEnabled((boolean)mParams.get(EXTRA_TORCH_ENABLED));

        mMethodChannel = new MethodChannel(registrar.messenger(), "me.hetian.flutter_qr_reader.reader_view_" + id);
        mMethodChannel.setMethodCallHandler(this);
    }

    @Override
    public View getView() {
        return decoratedBarcodeView;
    }

    @Override
    public void dispose() {
        decoratedBarcodeView = null;
        mParams = null;
        mRegistrar = null;
    }

//    @Override
//    public void onQRCodeRead(String text, PointF[] points) {
//        HashMap<String, Object> rest = new HashMap<String, Object>();
//        rest.put("text", text);
//        ArrayList<String> poi = new ArrayList<String>();
//        for (PointF point : points) {
//            poi.add(point.x + "," + point.y);
//        }
//        rest.put("points", poi);
//        mMethodChannel.invokeMethod("onQRCodeRead", rest);
//    }

    boolean flashlight;
    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "flashlight":
//                _view.setTorchEnabled(!flashlight);
//                flashlight = !flashlight;
//                result.success(flashlight);
                break;
            case "startCamera":
                decoratedBarcodeView.resume();
                break;
            case "stopCamera":
                decoratedBarcodeView.pause();
                break;
        }
    }

    class ScannCallback implements BarcodeCallback {

        @Override
        public void barcodeResult(final BarcodeResult result) {

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    HashMap<String, Object> rest = new HashMap<String, Object>();
                    rest.put("text", result.getText());
                    mMethodChannel.invokeMethod("onQRCodeRead", rest);

                    decoratedBarcodeView.pauseAndWait();
                } // This is your code
            };

            mainHandler.post(myRunnable);

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    }

}
