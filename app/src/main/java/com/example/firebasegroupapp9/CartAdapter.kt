package com.example.firebasegroupapp9


import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class CartAdapter(options: FirebaseRecyclerOptions<Cart>, private var ttlamount: TextView) :
    FirebaseRecyclerAdapter<Cart, CartAdapter.MyViewHolder>(options) {

    //    private var totalItemTextView: TextView
    private var totalAmount: Double = 0.0
//    private var totalItem: Int = 0


    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.cart_row_layout, parent, false)) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        val txtTotalPrice: TextView = itemView.findViewById(R.id.txtTotalPrice)
        val btnAdd: Button = itemView.findViewById(R.id.btnAdd)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        updateTotalAmount()
        return MyViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
        model: Cart
    ) {
        holder.txtName.text = model.name
        holder.txtPrice.text = "$" + model.price.toString() + "/ea"
        holder.txtQuantity.text = model.quantity.toString()
        holder.txtTotalPrice.text = "Total: " + String.format("$%.2f",model.quantity * model.price)
        val imageUrl = model.url
        val storageRef: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        Glide.with(holder.imgProduct.context).load(storageRef).into(holder.imgProduct)
        updateTotalAmount()

        holder.btnAdd.setOnClickListener { view ->
            val quantity = model.quantity
            if (quantity < 9) {
                val updatedQuantity = quantity + 1
                val databaseReference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("cart").child(
                        FirebaseAuth.getInstance().currentUser?.uid.toString()
                    )
                databaseReference.child(model.id).child("quantity").setValue(updatedQuantity)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            holder.txtQuantity.text = updatedQuantity.toString()
                            holder.txtTotalPrice.text =
                                "Total: " + String.format("$%.2f", updatedQuantity * model.price)
                        }
                    }.addOnFailureListener {
                        Log.e("Error", it.localizedMessage)
                        Toast.makeText(view.context, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                updateTotalAmount()
            } else {
                Toast.makeText(view.context, "Maximum quantity limit reached", Toast.LENGTH_SHORT).show()
            }
        }


        holder.btnRemove.setOnClickListener { view ->
            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("cart").child(
                    FirebaseAuth.getInstance().currentUser?.uid.toString()
                )
            val quantity = model.quantity - 1
            if (quantity == 0) {
                databaseReference.child(model.id).removeValue()
                val intent = Intent(
                    view.context,
                    CartActivity::class.java
                )
                view.context.startActivity(intent)
            } else {
                databaseReference.child(model.id).child("quantity").setValue(quantity)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            holder.txtQuantity.text = quantity.toString()
                            holder.txtTotalPrice.text =
                                "Total: " + String.format("$%.2f", quantity * model.price)
                        }
                    }.addOnFailureListener {
                        Log.e("Error", it.localizedMessage)
                        Toast.makeText(view.context, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
            updateTotalAmount()
        }
    }

    private fun updateTotalAmount() {
        totalAmount = calculateTotalAmount()
        val formattedTotal = String.format("$%.2f", totalAmount)
        ttlamount.text = formattedTotal
    }

    private fun calculateTotalAmount(): Double {
        var amount = 0.0
        for (i in 0 until itemCount) {
            val currentItem = getItem(i)
            if (currentItem != null) {
                amount += currentItem.price * currentItem.quantity
            }
        }
        return amount
    }
}