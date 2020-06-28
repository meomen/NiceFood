package com.vuducminh.nicefood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.util.DataUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vuducminh.nicefood.ViewHolder.ChatPictureHolder;
import com.vuducminh.nicefood.ViewHolder.ChatTextHolder;
import com.vuducminh.nicefood.callback.ILoadTimeFromFirebaseListener;
import com.vuducminh.nicefood.common.Common;
import com.vuducminh.nicefood.common.CommonAgr;
import com.vuducminh.nicefood.eventbus.HideFABCart;
import com.vuducminh.nicefood.model.ChatInfoModel;
import com.vuducminh.nicefood.model.ChatMessageModel;
import com.vuducminh.nicefood.model.OrderModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class ChatActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener {

    public static final int MY_CAMERA_REQUEST_CODE = 1999;
    public static final int MY_RESULT_LOAD_IMG = 6999;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.img_camera)
    ImageView img_camera;
    @BindView(R.id.img_image)
    ImageView img_image;
    @BindView(R.id.edt_chat)
    AppCompatEditText edt_chat;
    @BindView(R.id.img_send)
    ImageView img_send;
    @BindView(R.id.recycler_chat)
    RecyclerView recycler_chat;
    @BindView(R.id.img_preview)
    ImageView img_preview;

    private LinearLayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference chatRef,offsetRef;
    ILoadTimeFromFirebaseListener listener;

    FirebaseRecyclerAdapter<ChatMessageModel,RecyclerView.ViewHolder> adapter;
    FirebaseRecyclerOptions<ChatMessageModel> options;

    Uri fileUri;
    StorageReference storageReference;

    @OnClick(R.id.img_image)
    void onSelectImageClick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,MY_RESULT_LOAD_IMG);
    }

    @OnClick(R.id.img_camera)
    void onCaptureImageClick() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
        startActivityForResult(intent,MY_CAMERA_REQUEST_CODE);
    }


    @OnClick(R.id.img_send)
    void onSubmitChatClick() {
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeInMs = System.currentTimeMillis() + offset;

                listener.onLoadOnlyTimeSuccess(estimatedServerTimeInMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLoadtimeFailed(error.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        if(adapter != null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null)
            adapter.startListening();
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();

        loadChatContent();
    }

    private void loadChatContent() {
        adapter = new FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>(options) {

            @Override
            public int getItemViewType(int position) {

                ChatMessageModel chatMessageModel = adapter.getItem(position);

                if(!chatMessageModel.getOwnId().equals(Common.currentUser.getUid())    // Tin nhắn cửa hàng, không có hình
                        && !chatMessageModel.isPicture()) {
                    return 0;
                }
                else if(!chatMessageModel.getOwnId().equals(Common.currentUser.getUid())   // Tin nhắn cửa hàng, có hình
                        && chatMessageModel.isPicture()) {
                    return 1;
                }
                else if(chatMessageModel.getOwnId().equals(Common.currentUser.getUid())   // Tin nhắn của mình, không có hình
                        && !chatMessageModel.isPicture()) {
                    return 2;
                }
                else if(chatMessageModel.getOwnId().equals(Common.currentUser.getUid())    // Tin nhắn của mình, có hình
                        && chatMessageModel.isPicture()) {
                    return 3;
                }
                else {
                    return 0;   // Default
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessageModel model) {
                if(holder instanceof ChatTextHolder) {
                    ChatTextHolder chatTextHolder = (ChatTextHolder) holder;
                    chatTextHolder.tv_email.setText(model.getName());
                    chatTextHolder.tv_chat_message.setText(model.getContent());
                    chatTextHolder.tv_time.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(),0).toString());
                }
                else {
                    ChatPictureHolder chatPictureHolder = (ChatPictureHolder) holder;
                    chatPictureHolder.tv_email.setText(model.getName());
                    chatPictureHolder.tv_chat_message.setText(model.getContent());
                    chatPictureHolder.tv_time.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(),0).toString());

                    Glide.with(ChatActivity.this)
                            .load(model.getPictureLink())
                            .into(chatPictureHolder.img_preview);
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view;
                if(viewType == 0) {
                    view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_message_text,viewGroup,false);
                    return new ChatTextHolder(view);
                }
                else if(viewType == 1) {
                    view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_message_picture,viewGroup,false);
                    return new ChatPictureHolder(view);
                }
                else if(viewType == 2) {
                    view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_my_message_text,viewGroup,false);
                    return new ChatTextHolder(view);
                }
                else if(viewType == 3) {
                    view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_my_message_picture,viewGroup,false);
                    return new ChatPictureHolder(view);
                }
                else {
                    view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_message_text,viewGroup,false);
                    return new ChatTextHolder(view);
                }
            }
        };
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int latsVisiablepostion = layoutManager.findLastCompletelyVisibleItemPosition();
                if(latsVisiablepostion == -1 ||
                        (positionStart >= (friendlyMessageCount-1) &&
                                latsVisiablepostion == (positionStart-1))) {
                    recycler_chat.scrollToPosition(positionStart);
                }
            }
        });

        recycler_chat.setAdapter(adapter);
    }

    private void initViews() {
        listener = this;
        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(CommonAgr.CHAT_REF);
        offsetRef = database.getReference(".info/serverTimeOffset");

        Query query = chatRef.child(Common.generateChatRoomId(Common.currentRestaurant.getUid(),
                Common.currentUser.getUid()))
                .child(CommonAgr.CHAT_DETAIL_REF);

        options = new FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class)
                .build();
        ButterKnife.bind(this);
        layoutManager= new LinearLayoutManager(this);
        recycler_chat.setLayoutManager(layoutManager);

        toolbar.setTitle(Common.currentRestaurant.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "NiceFood");

        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdir())
                return null;
        }

        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile = new File(new StringBuilder(mediaStorageDir.getPath())
        .append(File.separator)
        .append("IMG_")
        .append(time_stamp)
        .append("_")
                .append(new Random().nextInt())
                .append(".jpg")
                .toString());
        return mediaFile;
    }

    @Override
    public void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs) {

    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {
        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.setName(Common.currentUser.getName());
        chatMessageModel.setContent(edt_chat.getText().toString());
        chatMessageModel.setTimeStamp(estimateTimeInMs);
        chatMessageModel.setOwnId(Common.currentUser.getUid());

        if(fileUri == null) {
            chatMessageModel.setPicture(false);
            submitChatToFirebase(chatMessageModel,chatMessageModel.isPicture(),estimateTimeInMs);
        }
        else
            uploadPicture(fileUri,chatMessageModel,estimateTimeInMs);
    }

    private void submitChatToFirebase(ChatMessageModel chatMessageModel, boolean isPicture,long estimateTimeInMs) {
        chatRef.child(Common.generateChatRoomId(Common.currentRestaurant.getUid(),
                Common.currentUser.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            appendChat(chatMessageModel,isPicture,estimateTimeInMs);
                        }
                        else {
                            createChat(chatMessageModel,isPicture,estimateTimeInMs);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void appendChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        Map<String,Object> update_data = new HashMap<>();
        update_data.put("lastUpdate",estimateTimeInMs);
        if(isPicture)
            update_data.put("lastMessage","<Image>");
        else
            update_data.put("lastMessage",chatMessageModel.getContent());

        chatRef.child(Common.generateChatRoomId(Common.currentRestaurant.getUid(),
                Common.currentUser.getUid()))
                .updateChildren(update_data)
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task2 -> {
                    if(task2.isSuccessful()) {
                        chatRef.child(Common.generateChatRoomId(Common.currentRestaurant.getUid(),
                                Common.currentUser.getUid()))
                                .child(CommonAgr.CHAT_DETAIL_REF)
                                .push()
                                .setValue(chatMessageModel)
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ChatActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }).addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                edt_chat.setText("");
                                edt_chat.requestFocus();
                                if(adapter != null) {
                                    adapter.notifyDataSetChanged();
                                    if(isPicture) {
                                        fileUri = null;
                                        img_preview.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    private void createChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        ChatInfoModel chatInfoModel = new ChatInfoModel();
        chatInfoModel.setCreateaName(chatMessageModel.getName());
        if(isPicture)
            chatInfoModel.setLastMessage("<Image>");
        else
            chatInfoModel.setLastMessage(chatMessageModel.getContent());
        chatInfoModel.setLastUpdate(estimateTimeInMs);
        chatInfoModel.setCreateDate(estimateTimeInMs);

       chatRef.child(Common.generateChatRoomId(Common.currentRestaurant.getUid(),Common.currentUser.getUid()))
               .setValue(chatInfoModel)
               .addOnFailureListener(e -> Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show())
               .addOnCompleteListener(task2 -> {
                   if(task2.isSuccessful()) {
                       chatRef.child(Common.generateChatRoomId(Common.currentRestaurant.getUid(),
                               Common.currentUser.getUid()))
                               .child(CommonAgr.CHAT_DETAIL_REF)
                               .push()
                               .setValue(chatMessageModel)
                               .addOnFailureListener(e -> {
                                   Toast.makeText(ChatActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                               }).addOnCompleteListener(task -> {
                           if(task.isSuccessful()) {
                               edt_chat.setText("");
                               edt_chat.requestFocus();
                               if(adapter != null) {
                                   adapter.notifyDataSetChanged();
                                   if(isPicture) {
                                       fileUri = null;
                                       img_preview.setVisibility(View.GONE);
                                   }
                               }
                           }
                       });
                   }
               });
    }

    private void uploadPicture(Uri fileUri, ChatMessageModel chatMessageModel,long estimateTimeInMs) {
        if(fileUri != null) {
            AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
                    .setCancelable(false)
                    .setMessage("Please wait...")
                    .create();
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(),fileUri);
            String path = new StringBuilder(Common.currentRestaurant.getUid())
                    .append("/")
                    .append(fileName)
                    .toString();
            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fileUri);

            //Create task
            Task<Uri> task = uploadTask.continueWithTask(task1 -> {
               return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task12 -> {
                if(task12.isSuccessful()) {
                    String url = task12.getResult().toString();
                    dialog.dismiss();

                    chatMessageModel.setPicture(true);
                    chatMessageModel.setPictureLink(url);

                    submitChatToFirebase(chatMessageModel,chatMessageModel.isPicture(),estimateTimeInMs);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            });

        }
        else {
            Toast.makeText(this,"Image is empty",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadtimeFailed(String message) {
        Toast.makeText(ChatActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == MY_CAMERA_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                ExifInterface ei= null;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),fileUri);
                    ei = new ExifInterface(getContentResolver().openInputStream(fileUri));

                    int orientation = ei.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED
                    );
                    Bitmap rotateBitmap = null;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                        rotateBitmap = rotateBitmap(bitmap,90);
                        break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitmap = rotateBitmap(bitmap,180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitmap = rotateBitmap(bitmap,270);
                            break;
                        default:
                            rotateBitmap = bitmap;
                            break;
                    }
                    img_preview.setVisibility(View.VISIBLE);
                    img_preview.setImageBitmap(rotateBitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode == MY_RESULT_LOAD_IMG) {
            if(resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                    img_preview.setImageBitmap(selectedImage);
                    img_preview.setVisibility(View.VISIBLE);
                    fileUri = imageUri;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show();
                }
            }
            else
                Toast.makeText(this,"Please choose image",Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap rotateBitmap(Bitmap bitmap,int i) {
        Matrix matrix = new Matrix();
        matrix.postRotate(i);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,true);
    }
}