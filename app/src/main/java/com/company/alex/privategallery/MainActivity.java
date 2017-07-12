package com.company.alex.privategallery;

import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialcab.MaterialCab;
import com.company.alex.privategallery.adapters.ImagesAdapter;
import com.company.alex.privategallery.utils.Constants;
import com.company.alex.privategallery.utils.CustomPinActivity;
import com.company.alex.privategallery.utils.ImagesData;
import com.company.alex.privategallery.utils.MovedData;
import com.company.alex.privategallery.utils.Parameters;
import com.company.alex.privategallery.utils.Utils;

import com.github.orangegangsters.lollipin.lib.PinCompatActivity;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.Executors;

public class MainActivity extends PinCompatActivity implements ImagesAdapter.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, MaterialCab.Callback {

    private static final int REQUEST_CODE = 101;
    private static final String TAG = "data";
    private static final String PATH = "paths";

    private static final int REQUEST_CODE_ENABLE = 11;
    private static final String CLAVE_REGISTERED = "is_registered"; //Static para declarar una variable a nivel de clase, final porque no va a camniar

    int selectedColor;

    RelativeLayout parentLayout;
    Toolbar toolbar;
    private com.company.alex.privategallery.progressDialog.CustomProgressDialog progressDialog;
    private DragSelectRecyclerView mList;
    private ImagesAdapter imagesAdapter;
    private MaterialCab cab;
    private Parameters parameters = new Parameters();
    private StaggeredGridLayoutManager mLayoutManager;

    private ArrayList<ImagesData> imagesData = new ArrayList<>();

    private LinkedHashSet<ImagesData> uniqueImages = new LinkedHashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();

        selectedColor = fetchAccentColor();

        SharedPreferences sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE); //Se crear un fichero user.xml en un directorio de la aplicacion
        Boolean isRegistered = sharedPreferences.getBoolean(CLAVE_REGISTERED, false); //Cada vez que el usuario se meta en la aplicacion cojo un numero y lo sumo


        if (!isRegistered) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(CLAVE_REGISTERED, true);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, CustomPinActivity.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            startActivityForResult(intent, REQUEST_CODE_ENABLE);
        } else {
            initializer(savedInstanceState);
        }
    }

    void initializer(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        parentLayout = (RelativeLayout) findViewById(R.id.main_parentLayout);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        imagesAdapter = new ImagesAdapter(this, imagesData, this.getColumnCount(), this.parameters);
        imagesAdapter.setSelectionListener(this);

        mList = (DragSelectRecyclerView) findViewById(R.id.main_drag_view);
        mList.setLayoutManager(new StaggeredGridLayoutManager(this.getColumnCount(), 1));
        mList.setAdapter(imagesAdapter);

        cab = MaterialCab.restoreState(savedInstanceState, this, this);

        init();
        checkAndDraw();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LinkedHashSet<ImagesData> arrayImages = new LinkedHashSet<>();
        if (resultCode == RESULT_OK) { //&& requestCode == READ_REQUEST_CODE
            appDirectory();//Crea carpeta para las fotos
            switch (requestCode) {
                case REQUEST_CODE_ENABLE:
                    Toast.makeText(this, "PinCode enabled", Toast.LENGTH_SHORT).show();
                    initializer(null);
                    break;
                case Constants.TYPE_MULTI_PICKER:
                    ArrayList<Image> imagesList = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                    for (int i = 0; i < imagesList.size(); i++) {
                        ImagesData imgData = new ImagesData(imagesList.get(i)._id, imagesList.get(i).uri, imagesList.get(i).imagePath, imagesList.get(i).isPortraitImage);
                        arrayImages.add(imgData);
                        }
                    imagesList.clear();
                    moveFile(new ArrayList<>(arrayImages), true);
                    arrayImages.clear();
                    break;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Log.i(TAG, "Permiso denegado por el usuario");
                else
                    Log.i(TAG, "Permiso concedido por el usuario");
                return;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        imagesAdapter.saveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Para utilizar los botones de la actionbar

        switch (item.getItemId()) {
            case R.id.addPhoto:
                intentGallery();
                break;
            case R.id.refresh:
                refreshAdapter();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshAdapter() {
        drawStaggeredView();
        ArrayList<ImagesData> array = new ArrayList<>(uniqueImages);
        for (int i = 0; i< array.size(); i++) {
            Log.i(PATH, array.get(i).imagePath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /////////////////////////////////////
    @Override
    public void onClick(int index) {
        imagesAdapter.toggleSelected(index);
    }

    @Override
    public void onLongClick(int index) {
        mList.setDragSelectActive(true, index);
    }

    @Override
    public void onDragSelectionChanged(int count) {
        //Log.i("data", "" + imagesAdapter.getSelectedCount());
        if (count > 0) {
            if (cab == null) {
                cab =
                        new MaterialCab(this, R.id.cab_stub)
                                .setMenu(R.menu.cab)
                                .setCloseDrawableRes(R.drawable.ic_close)
                                .start(this);
            }
            cab.setTitleRes(R.string.cab_title_x, count);
        } else if (cab != null && cab.isActive()) {
            cab.reset().finish();
            cab = null;
        }
    }
    //////////////////////////////////////////////


    private void init() {
        setSupportActionBar(toolbar);
        if (this.getIntent() != null && this.getIntent().hasExtra("PARAMS")) {
            Serializable object = this.getIntent().getSerializableExtra("PARAMS");
            if (object instanceof Parameters) {
                parameters = (Parameters) object;
            } else {
                Utils.showLongSnack(this.parentLayout, "Provided serializable data is not an instance of Params object.");
                setEmptyResult();
            }
        }
    }

    void checkPermissions() {
        if (hasStoragePermission()) {
            //TODO intent de lo que sea
        } else
            requestStoragePermissions();
    }

    public boolean hasStoragePermission() {
        int writePermissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        int readPermissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE");
        return Build.VERSION.SDK_INT < 23 || writePermissionCheck != -1 && readPermissionCheck != -1;
    }


    public void requestStoragePermissions() {
        int writePermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        ArrayList permissions = new ArrayList();

        if (writePermissions != 0) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readPermissions != 0) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, (String[]) permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
        }
    }

    private void setEmptyResult() {
        setResult(0);
        finish();
    }

    public void mediaScanner() {
        final ArrayList<ImagesData> allImagesData = new ArrayList<>(uniqueImages);
        try {
            for (int i = 0; i < allImagesData.size(); i++) {
                final int a = i;
                MediaScannerConnection.scanFile(this,

                        new String[]{allImagesData.get(i).imagePath}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i(PATH, "Scanned " + path + ":");
                                Log.i(PATH, "-> uri=" + uri);
                                if (uri == null) {

                                    uniqueImages.remove(allImagesData.get(a));
                                    ArrayList<ImagesData> prueba = new ArrayList<>(uniqueImages);
                                    for (int i = 0; i < prueba.size(); i++) {
                                        Log.i(PATH, "Teoricamente eliminado " + prueba.get(i).imagePath);
                                    }
                                }
                            }
                        });
            }
        } finally {
            refreshAdapter();
        }
    }

    public void checkAndDraw() {
        File f = new File(Environment.getExternalStorageDirectory() + "/PrivateGallery");// Comprobamos si la carpeta está ya creada
        if (f.isDirectory()) {
            (new MainActivity.ApiSimulator(this)).executeOnExecutor(Executors.newSingleThreadExecutor(), new Void[0]);
            return;
        }
    }

    public void drawStaggeredView() {
        if (uniqueImages.isEmpty()) {
            mList.setVisibility(View.GONE);
        } else { //TODO Arreglar para que una vez creado no lo defina siempre

            ArrayList<ImagesData> all = new ArrayList<>(uniqueImages);
            ArrayList<ImagesData> priv = new ArrayList<>();

            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).imagePath.contains("PrivateGallery")) {
                    priv.add(all.get(i));
                }
            }
            mLayoutManager = new StaggeredGridLayoutManager(this.getColumnCount(), 1);
            mLayoutManager.setGapStrategy(2);
            imagesAdapter.setItems(priv);
            imagesAdapter.setSelectionListener(this);
            mList.setLayoutManager(mLayoutManager);
            mList.setAdapter(imagesAdapter);
        }
    }

    private int getColumnCount() {
        if (parameters.getColumnCount() != 0) {
            return parameters.getColumnCount();
        } else {
            DisplayMetrics displayMetrics;
            float screenWidthInDp;
            if (parameters.getThumbnailWidthInDp() != 0) {
                displayMetrics = this.getResources().getDisplayMetrics();
                screenWidthInDp = (float) displayMetrics.widthPixels / displayMetrics.density;
                return (int) (screenWidthInDp / (float) parameters.getThumbnailWidthInDp());
            } else {
                displayMetrics = this.getResources().getDisplayMetrics();
                screenWidthInDp = (float) displayMetrics.widthPixels / displayMetrics.density;
                float thumbnailDpWidth = this.getResources().getDimension(com.vlk.multimager.R.dimen.thumbnail_width) / displayMetrics.density;
                return (int) (screenWidthInDp / thumbnailDpWidth);
            }
        }
    }

    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public void intentGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        Params params = new Params(); //Params de la libreria
        params.setToolbarColor(selectedColor);
        params.setCaptureLimit(100);
        params.setPickerLimit(100);
        intent.putExtra(Constants.KEY_PARAMS, params);


        startActivityForResult(intent, Constants.TYPE_MULTI_PICKER);
    }
    public void appDirectory() {

        File f = new File(Environment.getExternalStorageDirectory() + "/PrivateGallery");// Comprobamos si la carpeta está ya creada
        //Si no lo está la creamos
        if (!f.isDirectory()) {
            String newFolder = "/PrivateGallery";
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File myNewFolder = new File(extStorageDirectory + newFolder);
            myNewFolder.mkdir(); //creamos la carpeta
            // Log.d(TAG, "" + myNewFolder);
/*            File output = new File(f, ".nomedia");

            try {
                if (output.createNewFile()) {
                    Log.d(TAG, "NoMedia creado");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        } else {
            Log.d(TAG, "La carpeta ya estaba creada");
        }
    }

    public void moveFile(final ArrayList<ImagesData> images, boolean bool) {

        for (int i = 0; i < images.size(); i++) {
            final int a = i;
            File file_Source = new File(images.get(i).imagePath);
            File file_Destination;
            if (bool)
                file_Destination = new File(Environment.getExternalStorageDirectory() + "/PrivateGallery/" + new File(images.get(i).imagePath).getName());
            else
                file_Destination = new File(Environment.getExternalStorageDirectory() + "/Download/" + new File(images.get(i).imagePath).getName());
            Log.i(TAG, "Source " + file_Source + " destination " + file_Destination);


            FileChannel source = null;
            FileChannel destination = null;
            try {
                try {
                    source = new FileInputStream(file_Source).getChannel();
                    destination = new FileOutputStream(file_Destination).getChannel();

                    long count = 0;
                    long size = source.size();
                    while ((count += destination.transferFrom(source, count, size - count)) < size)
                        ;
                    MediaScannerConnection.scanFile(this,
                            new String[]{file_Destination.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i(PATH, "Scanned " + path + ":");
                                    Log.i(PATH, "-> uri=" + uri);
                                    MovedData mov = new MovedData(uri, path);
                                    uniqueImages.add(new ImagesData(mov.getmID(), mov.getmUri(), mov.getmPath(), images.get(a).isPortraitImage));
                                }
                            });
                    file_Source.delete();
                } finally {
                    if (source != null) {
                        source.close();
                    }
                    if (destination != null) {
                        destination.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaScanner();
    }

    public void showProgressDialog(String message) {
        if (this.progressDialog == null) {
            progressDialog = new com.company.alex.privategallery.progressDialog.CustomProgressDialog(this);
            //this.progressDialog = new CustomProgressDialog(this);
        }
        this.progressDialog.setMessage(message);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        ArrayList<ImagesData> selectedPhotos = new ArrayList<>();

        if (item.getItemId() == R.id.done) {
            for (int i = 0; i < imagesAdapter.getSelectedCount(); i++) {
                selectedPhotos.add(imagesAdapter.getData(i));
            }
            moveFile(selectedPhotos, false);
            drawStaggeredView();
            imagesAdapter.clearSelected();
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (imagesAdapter.getSelectedCount() > 0) {
            imagesAdapter.clearSelected();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        imagesAdapter.clearSelected();
        return true;
    }


    public class ApiSimulator extends AsyncTask<Void, Void, ArrayList<ImagesData>> {
        Activity context;
        String error = "";

        public ApiSimulator(Activity context) {
            this.context = context;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity.this.showProgressDialog("Loading..");
        }

        protected ArrayList<ImagesData> doInBackground(@NonNull Void... voids) { //TODO arreglar el cursor que tarda en "refrescar"
            Cursor imageCursor = null;

            try {
                String[] e = new String[]{"_id", "_data", "date_added", "height", "width"};
                String orderBy = "date_added DESC";
                imageCursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, e, null, null, orderBy);

                if (imageCursor.moveToFirst()) {
                    long id;
                    int height;
                    int width;
                    String imagePath;
                    Uri uri;
                    int idColumn = imageCursor.getColumnIndex("_id");
                    int heightColumn = imageCursor.getColumnIndex("height");
                    int widthColumn = imageCursor.getColumnIndex("width");
                    int dataColumn = imageCursor.getColumnIndex("_data");

                    do {
                        id = imageCursor.getLong(idColumn);
                        imagePath = imageCursor.getString(dataColumn);
                        uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                        width = imageCursor.getInt(widthColumn);
                        height = imageCursor.getInt(heightColumn);

                        ImagesData totalImg = new ImagesData(id, uri, imagePath, height > width);
                        uniqueImages.add(totalImg);
                    } while (imageCursor.moveToNext());
                }

            } catch (Exception var16) {
                var16.printStackTrace();
                this.error = var16.toString();
            } finally {
                if (imageCursor != null && !imageCursor.isClosed()) {
                    imageCursor.close();
                }
            }
            return new ArrayList<>(uniqueImages);
        }

        protected void onPostExecute(ArrayList<ImagesData> images) {
            super.onPostExecute(images);


            MainActivity.this.dismissProgressDialog();
            if (!MainActivity.this.isFinishing()) {
                if (this.error.length() == 0) {
                    mediaScanner();
                    MainActivity.this.drawStaggeredView();
                } else {
                    Utils.showLongSnack(MainActivity.this.parentLayout, this.error);
                }
            }
        }
    }
}


 /*   public void conseguirUri() {
        ArrayList<ImagesData> images = new ArrayList<>();
        Cursor imageCursor = null;

        try {
            String[] e = new String[]{"_id", "_data", "date_added", "height", "width"};
            String orderBy = "date_added DESC";
            imageCursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, e, null, null, orderBy);

            if (imageCursor.moveToFirst()) {
                long id;
                int height;
                int width;
                String imagePath;
                Uri uri;
                int idColumn = imageCursor.getColumnIndex("_id");
                int heightColumn = imageCursor.getColumnIndex("height");
                int widthColumn = imageCursor.getColumnIndex("width");
                int dataColumn = imageCursor.getColumnIndex("_data");

                do {
                    id = imageCursor.getLong(idColumn);
                    imagePath = imageCursor.getString(dataColumn);
                    uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    width = imageCursor.getInt(widthColumn);
                    height = imageCursor.getInt(heightColumn);

                    String outputPath = Environment.getExternalStorageDirectory() + "/PrivateGallery";

                    if (outputPath.equalsIgnoreCase(new File(imagePath).getParent())) {
                        ImagesData movedImages = new ImagesData(id, uri, imagePath, height > width);
                        images.add(movedImages);
                    }
                } while (imageCursor.moveToNext());
            }
        } catch (Exception var16) {
            var16.printStackTrace();
        } finally {
            if (imageCursor != null && !imageCursor.isClosed()) {
                imageCursor.close();
            }
        }
        imagesData = images;
        drawStaggeredView(imagesData);
    }*/