import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.constants.Constants.ITEM_RECEIVED
import com.example.whatsappclone.constants.Constants.ITEM_SENT
import com.example.whatsappclone.models.ChatMessages
import com.google.firebase.auth.FirebaseAuth
import com.example.whatsappclone.databinding.ItemViewReceiverBinding
import com.example.whatsappclone.databinding.ItemViewSenderBinding
import android.R.id.message
import android.annotation.SuppressLint
import android.widget.ImageView
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.ReactionsConfigBuilder
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.View.VIEW_LOG_TAG
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.MESSAGE_PHOTO
import com.example.whatsappclone.constants.Constants.NODE_NAME_CHATS
import com.example.whatsappclone.constants.Constants.NODE_NAME_MESSAGES
import com.google.firebase.database.FirebaseDatabase
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
import android.widget.LinearLayout


class AdapterRvMessages(
    var context: Context,
    var messagesData: ArrayList<ChatMessages>,
    var senderRoom: String,
    var receiverRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var deCipher: Cipher? = null
    private var secretKeySpec: SecretKeySpec? = null
    var isImageFitToScreen = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            var view =
                LayoutInflater.from(context).inflate(R.layout.item_view_sender, parent, false)
            SentViewHolder(view)
        } else {
            var view =
                LayoutInflater.from(context).inflate(R.layout.item_view_receiver, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        var message = messagesData[position]
        return if (FirebaseAuth.getInstance().uid.equals(message.senderId)) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messagesData.size
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemViewSenderBinding? = ItemViewSenderBinding.bind(itemView)
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemViewReceiverBinding? = ItemViewReceiverBinding.bind(itemView)
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageObject = messagesData[position]
        // Array of the reactions.
        var reactions = arrayOf(
            R.drawable.ic_fb_like, R.drawable.ic_fb_love, R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow, R.drawable.ic_fb_sad, R.drawable.ic_fb_angry
        )
        val config = reactionConfig(context) {
            reactions {
                resId { R.drawable.ic_fb_like }
                resId { R.drawable.ic_fb_love }
                resId { R.drawable.ic_fb_laugh }
                reaction { R.drawable.ic_fb_wow scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_sad scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_angry scale ImageView.ScaleType.FIT_XY }
            }
        }
        // Reactions Popup
        val popup = ReactionPopup(context, config) { pos ->
            true.also {
                if (holder.javaClass === SentViewHolder::class.java) {
                    val viewHolder = holder as SentViewHolder
                    viewHolder.binding!!.ivFeeling.setImageResource(reactions[pos])
                    viewHolder.binding!!.ivFeeling.visibility = View.VISIBLE
                } else {
                    val viewHolder = holder as ReceivedViewHolder
                    viewHolder.binding!!.ivFeeling.setImageResource(reactions[pos])
                    viewHolder.binding!!.ivFeeling.visibility = View.VISIBLE
                }
                messageObject.feeling = pos + 1

                // Updating the feeling to the database.
                FirebaseDatabase.getInstance().reference
                    .child(NODE_NAME_CHATS)
                    .child(senderRoom)
                    .child(NODE_NAME_MESSAGES)
                    .child(messageObject.messageId).setValue(messageObject)
                FirebaseDatabase.getInstance().reference
                    .child(NODE_NAME_CHATS)
                    .child(receiverRoom)
                    .child(NODE_NAME_MESSAGES)
                    .child(messageObject.messageId).setValue(messageObject)
            }
        }


        if (holder.javaClass === SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            if (messageObject.message == MESSAGE_PHOTO) {
                viewHolder.binding!!.ivSentImage.visibility = View.VISIBLE
                viewHolder.binding!!.tvSentMessage.visibility = View.GONE
                Glide.with(context).load(messageObject.messageUrl)
                    .placeholder(R.drawable.ic_placeholder).into(viewHolder.binding!!.ivSentImage)
            }
            viewHolder.binding!!.tvSentMessage.text = decryptMessage(messageObject.message)
            val dateFormat = SimpleDateFormat(Constants.DATE_PATTERN)
            viewHolder.binding!!.tvTimeStamp.text =
                dateFormat.format(Date(messageObject.timeStamp!!))
            if (messageObject.feeling > 0) {
                viewHolder.binding!!.ivFeeling.setImageResource(reactions[messageObject.feeling - 1])
                viewHolder.binding!!.ivFeeling.visibility = View.VISIBLE
            } else {
                viewHolder.binding!!.ivFeeling.visibility = View.GONE
            }

            viewHolder.binding!!.container.setOnTouchListener { p0, p1 ->
//                popup.onTouch(p0!!, p1!!)
                false
            }
        } else {
            val viewHolder = holder as ReceivedViewHolder
            if (messageObject.message == MESSAGE_PHOTO) {
                viewHolder.binding!!.ivReceivedImage.visibility = View.VISIBLE
                viewHolder.binding!!.tvReceiverMessage.visibility = View.GONE
                Glide.with(context).load(messageObject.messageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(viewHolder.binding!!.ivReceivedImage)


            }
            viewHolder.binding!!.tvReceiverMessage.text = decryptMessage(messageObject.message)
            val dateFormat = SimpleDateFormat(Constants.DATE_PATTERN)
            viewHolder.binding!!.tvTimeStamp.text =
                dateFormat.format(Date(messageObject.timeStamp!!))
            if (messageObject.feeling > 0) {
                viewHolder.binding!!.ivFeeling.setImageResource(reactions[messageObject.feeling - 1])
                viewHolder.binding!!.ivFeeling.visibility = View.VISIBLE
            } else {
                viewHolder.binding!!.ivFeeling.visibility = View.GONE
            }
            viewHolder.binding!!.container.setOnTouchListener { p0, p1 ->
//                popup.onTouch(p0!!, p1!!)
                false
            }
        }
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

    private fun fullIamge() {

    }

}