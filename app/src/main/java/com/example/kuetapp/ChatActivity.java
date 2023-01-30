 package com.example.kuetapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.content.Context;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

 public class ChatActivity extends AppCompatActivity
 {
     private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

     private TextView userName, userLastSeen;
     private CircleImageView userImage;

     private Toolbar ChatToolBar;
     private FirebaseAuth mAuth;
     private DatabaseReference RootRef;

     private ImageButton SendMessageButton, SendFilesButton;
     private EditText MessageInputText;

     private final List<Messages> messagesList = new ArrayList<>();
     private LinearLayoutManager linearLayoutManager;
     private MessageAdapter messageAdapter;
     private RecyclerView userMessagesList;


     private String saveCurrentTime, saveCurrentDate;

     private TextView appbarText;
     public FirebaseUser currentUser;
     String currentUserID;

     private Uri fileUri;
     private String myUrl="";
     private StorageTask uploadTask;

     private TextView testText;



     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat);

         appbarText = (TextView) findViewById(R.id.appbar_text);
         appbarText.setVisibility(View.INVISIBLE);
         appbarText.setText("");


         mAuth = FirebaseAuth.getInstance();
         messageSenderID = mAuth.getCurrentUser().getUid();
         RootRef = FirebaseDatabase.getInstance().getReference();

         mAuth = FirebaseAuth.getInstance();
         currentUser = mAuth.getCurrentUser();

             currentUserID = mAuth.getCurrentUser().getUid();


         messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
         messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
         messageReceiverImage = getIntent().getExtras().get("visit_image").toString();


         IntializeControllers();




         userName.setText(messageReceiverName);
         Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

         MessageInputText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String messageText = MessageInputText.getText().toString().trim();
                 Toast.makeText(ChatActivity.this, " I am clicked ", Toast.LENGTH_SHORT).show();
             }
         });


         SendMessageButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {
                 SendMessage();
             }
         });


         DisplayLastSeen();


         SendFilesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent();
                 intent.setAction(Intent.ACTION_GET_CONTENT);
                 intent.setType("image/*");
                 startActivityForResult(intent.createChooser(intent,"Select Image"),438);
             }
         });
     }




     private void IntializeControllers()
     {
         ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
         setSupportActionBar(ChatToolBar);

         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setDisplayShowCustomEnabled(true);

         LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
         actionBar.setCustomView(actionBarView);

         userName = (TextView) findViewById(R.id.custom_profile_name);
         userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
         userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

         SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
         SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
         MessageInputText = (EditText) findViewById(R.id.input_message);

         messageAdapter = new MessageAdapter(messagesList);
         userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
         linearLayoutManager = new LinearLayoutManager(this);
         userMessagesList.setLayoutManager(linearLayoutManager);
         userMessagesList.setAdapter(messageAdapter);




         Calendar calendar = Calendar.getInstance();

         SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
         saveCurrentDate = currentDate.format(calendar.getTime());

         SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
         saveCurrentTime = currentTime.format(calendar.getTime());
     }


     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
             fileUri = data.getData();

             StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

             String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
             String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

             DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                     .child(messageSenderID).child(messageReceiverID).push();

             final String messagePushID = userMessageKeyRef.getKey();

             StorageReference filePath = storageReference.child(messagePushID +".jpg");

             uploadTask = filePath.putFile(fileUri);

             uploadTask.continueWithTask(new Continuation() {
                 @Override
                 public Object then(@NonNull Task task) throws Exception {
                     if (!task.isSuccessful()){
                         throw task.getException();
                     }

                     return filePath.getDownloadUrl();
                 }
             }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                 @Override
                 public void onComplete(@NonNull Task<Uri> task) {
                     if (task.isSuccessful()){
                         Uri downloadUrl = task.getResult();

                         myUrl = downloadUrl.toString();


                         Map messageTextBody = new HashMap();
                         messageTextBody.put("message", myUrl);
                         messageTextBody.put("memoryLocation", "");
                         messageTextBody.put("name", fileUri.getLastPathSegment());
                         messageTextBody.put("type", "Image");
                         messageTextBody.put("from", messageSenderID);
                         messageTextBody.put("to", messageReceiverID);
                         messageTextBody.put("messageID", messagePushID);
                         messageTextBody.put("time", saveCurrentTime);
                         messageTextBody.put("date", saveCurrentDate);

                         Map messageBodyDetails = new HashMap();
                         messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                         messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                         RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                             @Override
                             public void onComplete(@NonNull Task task)
                             {
                                 if (task.isSuccessful())
                                 {

                                 }
                                 else
                                 {
                                     Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                 }
                                 MessageInputText.setText("");
                             }
                         });
                     }
                 }
             });
         }
     }

     private void DisplayLastSeen()
     {



         RootRef.child("Users").child(messageReceiverID)
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot)
                     {
                         if (dataSnapshot.child("userState").hasChild("state"))
                         {
                             String state = dataSnapshot.child("userState").child("state").getValue().toString();
                             String date = dataSnapshot.child("userState").child("date").getValue().toString();
                             String time = dataSnapshot.child("userState").child("time").getValue().toString();

                             if (state.equals("online"))
                             {
                                 userLastSeen.setText("online");
                             }
                             else if (state.equals("offline"))
                             {
                                 userLastSeen.setText("Last Seen: " + time + "   " + date);
                             }
                         }
                         else
                         {
                             userLastSeen.setText("offline");
                         }
                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 });

         updateUserStatus("online");
     }


     @Override
     protected void onStart()
     {
         super.onStart();

         updateUserStatus("online");

         RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                 .addChildEventListener(new ChildEventListener() {
                     @Override
                     public void onChildAdded(DataSnapshot dataSnapshot, String s)
                     {
                         Messages messages = dataSnapshot.getValue(Messages.class);

                         messagesList.add(messages);

                         messageAdapter.notifyDataSetChanged();

                         userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                        // Toast.makeText(ChatActivity.this, "Number :"+userMessagesList.getAdapter().getItemCount(), Toast.LENGTH_SHORT).show();
                     }

                     @Override
                     public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                     }

                     @Override
                     public void onChildRemoved(DataSnapshot dataSnapshot) {

                     }

                     @Override
                     public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 });
     }



     private void SendMessage()
     {

         String messageText = MessageInputText.getText().toString().trim();

         if (TextUtils.isEmpty(messageText))
         {
             Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
         }
         else
         {
             String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
             String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

             DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                     .child(messageSenderID).child(messageReceiverID).push();

             String messagePushID = userMessageKeyRef.getKey();

             Map messageTextBody = new HashMap();
             messageTextBody.put("message", messageText);
             messageTextBody.put("type", "text");
             messageTextBody.put("from", messageSenderID);
             messageTextBody.put("to", messageReceiverID);
             messageTextBody.put("messageID", messagePushID);
             messageTextBody.put("time", saveCurrentTime);
             messageTextBody.put("date", saveCurrentDate);

             Map messageBodyDetails = new HashMap();
             messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
             messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

             RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                 @Override
                 public void onComplete(@NonNull Task task)
                 {
                     if (task.isSuccessful())
                     {

                     }
                     else
                     {
                         Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                     }
                     MessageInputText.setText("");
                 }
             });
         }
     }

     private void updateUserStatus(String state)
     {
         String saveCurrentTime, saveCurrentDate;

         Calendar calendar = Calendar.getInstance();

         SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
         saveCurrentDate = currentDate.format(calendar.getTime());

         SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
         saveCurrentTime = currentTime.format(calendar.getTime());

         HashMap<String, Object> onlineStateMap = new HashMap<>();
         onlineStateMap.put("time", saveCurrentTime);
         onlineStateMap.put("date", saveCurrentDate);
         onlineStateMap.put("state", state);

         RootRef.child("Users").child(messageSenderID).child("userState")
                 .updateChildren(onlineStateMap);

     }

 }