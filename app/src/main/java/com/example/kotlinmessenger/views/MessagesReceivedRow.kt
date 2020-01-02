package com.example.kotlinmessenger.views

import com.example.kotlinmessenger.ChatListActivity
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.message_received_row.view.*

class MessagesReceivedRow(val ChatMessage: ChatListActivity.ChatMessage): Item<ViewHolder>(){
        var chatPartnerUser:User? = null


    override fun getLayout(): Int {
        return R.layout.message_received_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_msgrcv_latest_message.text = ChatMessage.text

        val chatPartnerId: String
        if (ChatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = ChatMessage.toId
        }else {
            chatPartnerId = ChatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.textview_msgrcv_username.text = chatPartnerUser?.username

                val targetImageView = viewHolder.itemView.imageview_message_received_row
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }

        })






    }

}