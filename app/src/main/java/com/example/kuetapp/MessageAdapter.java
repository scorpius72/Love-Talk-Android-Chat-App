package com.example.kuetapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.OutputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    int viewCount=1;

    OutputStream outputStream;
    Bitmap bitmap;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText,messageReceiverMax, receiverMessageText,senderMessageTextTime,receiverMessageTextTime,senderImageTime,receiverImageTime,messageSenderMax;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            senderMessageTextTime = (TextView) itemView.findViewById(R.id.sender_messsage_text_time);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverMessageTextTime = (TextView) itemView.findViewById(R.id.receiver_message_text_time);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);

            senderImageTime = (TextView) itemView.findViewById(R.id.sender_messsage_image_time);
            receiverImageTime = (TextView) itemView.findViewById(R.id.receiver_message_image_time);

            messageSenderMax = (TextView) itemView.findViewById(R.id.sender_messsage_text_max);
            messageReceiverMax= (TextView) itemView.findViewById(R.id.receiver_message_text_max);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverMessageTextTime.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.senderMessageTextTime.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.receiverImageTime.setVisibility(View.GONE);
        messageViewHolder.senderImageTime.setVisibility(View.GONE);

        messageViewHolder.messageSenderMax.setVisibility(View.GONE);
        messageViewHolder.messageReceiverMax.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {


                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.messageSenderMax.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.messageSenderMax.setTextColor(Color.BLACK);
                messageViewHolder.messageSenderMax.setText(messages.getMessage());

                if (messages.getMessage().length()>=30){
                    messageViewHolder.messageSenderMax.setVisibility(View.VISIBLE);
                }
                else{
                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                }

                messageViewHolder.senderMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewCount==1) {
                            messageViewHolder.senderMessageTextTime.setVisibility(View.VISIBLE);
                            viewCount=0;
                        }
                        else {
                            messageViewHolder.senderMessageTextTime.setVisibility(View.GONE);
                            viewCount=1;
                        }
                    }
                });
                messageViewHolder.messageSenderMax.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewCount==1) {
                            messageViewHolder.senderMessageTextTime.setVisibility(View.VISIBLE);
                            viewCount=0;
                        }
                        else {
                            messageViewHolder.senderMessageTextTime.setVisibility(View.GONE);
                            viewCount=1;
                        }
                    }
                });

                messageViewHolder.senderMessageTextTime.setBackgroundResource(R.drawable.sender_message_time_layout);
                messageViewHolder.senderMessageTextTime.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageTextTime.setText(messages.getTime()+"-"+messages.getDate());


                // testing for long click listener

                messageViewHolder.senderMessageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                       // Toast.makeText(messageViewHolder.senderMessageText.getContext(), "Hey why do you click me??", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = new AlertDialog.Builder(messageViewHolder.senderMessageText.getContext())
                                .setTitle("Hey Don't Delete Me..")
                                .setMessage("Click ok inorder to delete me .. but i say don't do this")
                                .setPositiveButton("Delete",null)
                                .setNegativeButton("Cancel",null)
                                .show();

                        Button positiveButton =  dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteSentMessages(i,messageViewHolder);
                                deleteReceiverMessages(i,messageViewHolder);
                                dialog.dismiss();
                            }
                        });

                        Button negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        return false;
                    }
                });

                // for the text message max ..

                messageViewHolder.messageSenderMax.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //Toast.makeText(messageViewHolder.messageSenderMax.getContext(), "Hey why do you click me??", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = new AlertDialog.Builder(messageViewHolder.messageSenderMax.getContext())
                                .setTitle("Hey Don't Delete Me..")
                                .setMessage("Click ok inorder to delete me .. but i say don't do this")
                                .setPositiveButton("Delete",null)
                                .setNegativeButton("Cancel",null)
                                .show();

                        Button positiveButton =  dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteSentMessages(i,messageViewHolder);
                                deleteReceiverMessages(i,messageViewHolder);
                                dialog.dismiss();
                            }
                        });

                        Button negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        return false;
                    }
                });


            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());


                messageViewHolder.messageReceiverMax.setText(messages.getMessage());

                if (messages.getMessage().length()>=30){
                    messageViewHolder.messageReceiverMax.setVisibility(View.VISIBLE);
                }
                else{
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                }

                messageViewHolder.receiverMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewCount==1) {
                            messageViewHolder.receiverMessageTextTime.setVisibility(View.VISIBLE);
                            viewCount=0;
                        }
                        else {
                            messageViewHolder.receiverMessageTextTime.setVisibility(View.GONE);
                            viewCount=1;
                        }
                    }
                });

                messageViewHolder.messageReceiverMax.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewCount==1) {
                            messageViewHolder.receiverMessageTextTime.setVisibility(View.VISIBLE);
                            viewCount=0;
                        }
                        else {
                            messageViewHolder.receiverMessageTextTime.setVisibility(View.GONE);
                            viewCount=1;
                        }
                    }
                });

                messageViewHolder.receiverMessageTextTime.setBackgroundResource(R.drawable.receiver_message_time_layout);
                messageViewHolder.receiverMessageTextTime.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageTextTime.setText(messages.getTime()+"-"+messages.getDate());


                // for the receiver msg delete

                messageViewHolder.receiverMessageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                       // Toast.makeText(messageViewHolder.receiverMessageText.getContext(), "Hey why do you click me??", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = new AlertDialog.Builder(messageViewHolder.receiverMessageText.getContext())
                                .setTitle("Hey Don't Delete Me..")
                                .setMessage("Click ok inorder to delete me .. but i say don't do this")
                                .setPositiveButton("Delete",null)
                                .setNegativeButton("Cancel",null)
                                .show();

                        Button positiveButton =  dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteSentMessages(i,messageViewHolder);
                                deleteReceiverMessages(i,messageViewHolder);
                                dialog.dismiss();
                            }
                        });

                        Button negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        return false;
                    }
                });

                // for receiver msg max delete

                messageViewHolder.messageReceiverMax.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //Toast.makeText(messageViewHolder.messageReceiverMax.getContext(), "Hey why do you click me??", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = new AlertDialog.Builder(messageViewHolder.messageReceiverMax.getContext())
                                .setTitle("Hey Don't Delete Me..")
                                .setMessage("Click ok inorder to delete me .. but i say don't do this")
                                .setPositiveButton("Delete",null)
                                .setNegativeButton("Cancel",null)
                                .show();

                        Button positiveButton =  dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteSentMessages(i,messageViewHolder);
                                deleteReceiverMessages(i,messageViewHolder);
                                dialog.dismiss();
                            }
                        });

                        Button negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        return false;
                    }
                });
            }
        }
        // for the image..........

        else if (fromMessageType.equals("Image")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);



                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                

                messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            Intent profileIntent = new Intent(messageViewHolder.itemView.getContext(),imageViewLayout.class);
                            profileIntent.putExtra("url",messages.getMessage());
                            messageViewHolder.itemView.getContext().startActivity(profileIntent);

                    }
                });

                messageViewHolder.senderImageTime.setBackgroundResource(R.drawable.sender_message_time_layout);
                messageViewHolder.senderImageTime.setTextColor(Color.BLACK);
                messageViewHolder.senderImageTime.setText(messages.getTime()+"-"+messages.getDate());


                // for deleting the image

                messageViewHolder.messageSenderPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                       // Toast.makeText(messageViewHolder.messageSenderPicture.getContext(), "Hey why do you click me??", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = new AlertDialog.Builder(messageViewHolder.messageSenderPicture.getContext())
                                .setTitle("Hey Don't Delete Me..")
                                .setMessage("Click ok inorder to delete me .. but i say don't do this")
                                .setPositiveButton("Delete",null)
                                .setNegativeButton("Cancel",null)
                                .show();

                        Button positiveButton =  dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteSentMessages(i,messageViewHolder);
                                deleteReceiverMessages(i,messageViewHolder);
                                dialog.dismiss();
                            }
                        });

                        Button negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        return false;
                    }
                });
            }
            else{
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);


                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

                messageViewHolder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(messageViewHolder.itemView.getContext(),imageViewLayout.class);
                        profileIntent.putExtra("url",messages.getMessage());
                        messageViewHolder.itemView.getContext().startActivity(profileIntent);
                    }
                });
                messageViewHolder.receiverImageTime.setBackgroundResource(R.drawable.receiver_message_time_layout);
                messageViewHolder.receiverImageTime.setTextColor(Color.BLACK);
                messageViewHolder.receiverImageTime.setText(messages.getTime()+"-"+messages.getDate());

                // for deleting the image

                messageViewHolder.messageReceiverPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                       // Toast.makeText(messageViewHolder.messageReceiverPicture.getContext(), "Hey why do you click me??", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = new AlertDialog.Builder(messageViewHolder.messageReceiverPicture.getContext())
                                .setTitle("Hey Don't Delete Me..")
                                .setMessage("Click ok inorder to delete me .. but i say don't do this")
                                .setPositiveButton("Delete",null)
                                .setNegativeButton("Cancel",null)
                                .show();

                        Button positiveButton =  dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteSentMessages(i,messageViewHolder);
                                deleteReceiverMessages(i,messageViewHolder);
                                dialog.dismiss();
                            }
                        });

                        Button negativeButton =  dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        return false;
                    }
                });
            }
        }
    }

    private void deleteSentMessages (final int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully from sender", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "There is an Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiverMessages (final int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully from receiver", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "There is an Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

}