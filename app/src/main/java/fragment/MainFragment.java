package fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloudhouse.R;
import com.example.cloudhouse.ShoeCabinetActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Adapter.MainAdapter;
import task.UploadTask;
import util.FileUtil;

public class MainFragment extends Fragment implements UploadTask.OnUploadListener {
    private MainAdapter mAdapter;
    private final int REQUEST_CODE_CAMERA = 1;
    private final int REQUEST_CODE_ALBUM = 2;
    private Uri mImageUri;
    private String mFilePath;
    private final List<String> data1 = new ArrayList<>();
    private final List<Integer> data2 = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView mRecyclerView = requireView().findViewById(R.id.recycler_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new MainAdapter(getActivity());

        data1.add("??????");
        data1.add("??????");
        data1.add("??????");
        data1.add("??????");
        data1.add("??????");
        data1.add("????????????");

        data2.add(R.mipmap.main_first);
        data2.add(R.mipmap.main_second);
        data2.add(R.mipmap.main_third);
        data2.add(R.mipmap.main_fouth);
        data2.add(R.mipmap.main_fifth);
        data2.add(R.mipmap.main_six);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setOnItemClickListener((position, arg, size, list) -> {
            if (arg == 1) {
                list.add(position, "????????????");

                View bottomView = View.inflate(requireContext(), R.layout.popwindow_layout, null);
                TextView mAlbum = bottomView.findViewById(R.id.tv_album);
                TextView mCancel = bottomView.findViewById(R.id.tv_cancel);
                TextView mCamera = bottomView.findViewById(R.id.tv_camera);

                PopupWindow popupWindow = new PopupWindow(bottomView, -1, -2);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setContentView(bottomView);

                @SuppressLint("NonConstantResourceId")
                View.OnClickListener clickListener = v -> {
                    int id = v.getId();
                    switch (id){
                        case R.id.tv_camera:
                            Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Video.Media.DISPLAY_NAME, "photo_");
                            values.put(MediaStore.Video.Media.MIME_TYPE, "image/jpeg");

                            mImageUri = requireContext().getContentResolver()
                                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                            startActivityForResult(photoIntent, REQUEST_CODE_CAMERA);
                            popupWindow.dismiss();
                            break;

                        case R.id.tv_album:
                            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // ??????????????????
                            albumIntent.setType("image/*"); // ???????????????
                            startActivityForResult(albumIntent, REQUEST_CODE_ALBUM);
                            popupWindow.dismiss();
                            break;

                        case R.id.tv_cancel:
                            popupWindow.dismiss();
                            break;
                    }
                };

                mAlbum.setOnClickListener(clickListener);
                mCamera.setOnClickListener(clickListener);
                mCancel.setOnClickListener(clickListener);

                @SuppressLint("InflateParams")
                View rootView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_main, null);
                popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

                data2.add(position, R.mipmap.main_first);
                mAdapter.notifyItemInserted(position);
                mAdapter.notifyItemRangeChanged(position, list.size() - position);
            }
            else if (arg == 2) {
                Intent intent = new Intent(requireActivity(), ShoeCabinetActivity.class);
                startActivity(intent);
            } else {
                mAdapter.showDialog(requireContext(), list, position);
            }
        });

        mAdapter.setDataSource(data1);
        mAdapter.setImageSource(data2);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_ALBUM) {
            assert data != null;
            if (data.getData() != null) { // ???????????????????????????
                Uri uri = data.getData(); // ?????????????????????????????????
                // ?????????????????????????????????
                mFilePath = String.format("%s/%s.jpg",
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_" + getNowDateTime());
                FileUtil.saveFileFromUri(requireContext(), uri, mFilePath); // ?????????????????????
                UploadTask task = new UploadTask(); // ????????????????????????
                task.setOnUploadListener(this); // ???????????????????????????
                task.execute(mFilePath); // ??????????????????????????????????????????
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getNowDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

    @Override
    public void finishUpload(String result) {
        String desc = String.format("????????????????????????%s\n???????????????%s\n?????????????????????%s%s",
                mFilePath, (TextUtils.isEmpty(result)) ? "??????" : result,
                "http://106.52.165.70:8080/user/profile", mFilePath.substring(mFilePath.lastIndexOf("/")));
        Log.d("MainFragment", desc);
    }
}
