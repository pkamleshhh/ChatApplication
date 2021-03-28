import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.activities.ChatActivity
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.DATE_PATTERN
import com.example.whatsappclone.constants.Constants.INTENT_KEY_FOR_NAME
import com.example.whatsappclone.constants.Constants.INTENT_KEY_FOR_PRO_PIC
import com.example.whatsappclone.constants.Constants.INTENT_KEY_FOR_UID
import com.example.whatsappclone.constants.Constants.MESSAGE_PHOTO
import com.example.whatsappclone.constants.Constants.NODE_NAME_CHATS
import com.example.whatsappclone.constants.Constants.NODE_NAME_MESSAGE
import com.example.whatsappclone.constants.Constants.NODE_NAME_MESSAGES
import com.example.whatsappclone.constants.Constants.NODE_NAME_TIME_STAMP
import com.example.whatsappclone.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList


class AdapterRvChatRows(
    private val context: Context,
    private val usersData: ArrayList<Users> = ArrayList(),
    private val itemClicked: ItemClicked
) :
    RecyclerView.Adapter<AdapterRvChatRows.ViewHolder>() {
    private var deCipher: Cipher? = null
    private var secretKeySpec: SecretKeySpec? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_rows, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return usersData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersData[position]
        val senderId = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference
            .child(NODE_NAME_CHATS)
            .child(senderId + user.userId)
            .child(NODE_NAME_MESSAGES)
            .orderByChild(NODE_NAME_TIME_STAMP)
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SimpleDateFormat")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (data: DataSnapshot in snapshot.children) {
                            var lastMsg = data.child(NODE_NAME_MESSAGE).value.toString()
                            val timeLastMsg =
                                data.child(NODE_NAME_TIME_STAMP).getValue(Long::class.java)
                            val dateFormat = SimpleDateFormat(DATE_PATTERN)
                            lastMsg = decryptMessage(lastMsg)
                            holder.tvLastMessage.text = lastMsg
                            if (lastMsg == MESSAGE_PHOTO) {
                                holder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.photo_icon_small,
                                    0,
                                    0,
                                    0
                                )
                            } else {

                            }
                            holder.tvTimeStamp.text = dateFormat.format(Date(timeLastMsg!!))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        holder.tvName.text = user.userName
        Glide.with(context).load(user.profilePic).placeholder(R.drawable.avatar)
            .into(holder.ivAvatar)
        holder.itemView.setOnClickListener(View.OnClickListener {
            itemClicked.onItemClicked(holder.adapterPosition)
        })
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, ChatActivity::class.java)
//            intent.putExtra(INTENT_KEY_FOR_NAME, user.userName)
//            intent.putExtra(INTENT_KEY_FOR_PRO_PIC, user.profilePic)
//            intent.putExtra(INTENT_KEY_FOR_UID, user.userId)
//            context.startActivity(intent)
//        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById<TextView>(R.id.tvPersonName)!!
        val tvLastMessage = itemView.findViewById<TextView>(R.id.tvLastMessage)!!
        val ivAvatar = itemView.findViewById<ImageView>(R.id.ivAvatar)!!
        val tvTimeStamp = itemView.findViewById<TextView>(R.id.tvTimeStamp)!!
    }

    interface ItemClicked {
        fun onItemClicked(position: Int)
    }

    private fun decryptMessage(msg: String): String {
        try {
            deCipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
        secretKeySpec = SecretKeySpec(Constants.ENCRYPTION_KEY, Constants.ENCRYPTION_ALGORITHM)
        val charSet = Charset
            .forName("ISO-8859-1")
        val encryptedByte: ByteArray = msg.toByteArray(charSet)
        var decryptedString = msg
        var decryption: ByteArray? = null
        try {
            deCipher!!.init(Cipher.DECRYPT_MODE, secretKeySpec)
            decryption = deCipher!!.doFinal(encryptedByte)
            decryptedString = String(decryption)
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        }
        return decryptedString
    }

}