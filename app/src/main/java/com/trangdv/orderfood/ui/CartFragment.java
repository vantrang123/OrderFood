package com.trangdv.orderfood.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.Database;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.model.Request;
import com.trangdv.orderfood.viewholder.CartAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    FirebaseDatabase database;
    DatabaseReference requests;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CartAdapter adapter;
    List<Order> cart = new ArrayList<>();
    static List<List<Order>> orderList = new ArrayList<>();

    TextView tvTotalPrice;
    Button btnPlace;
    float totalPrice;
    static float total;

    String latitude = "";
    String longitude = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Cart");
        View view = inflater.inflate(R.layout.activity_cart, container, false);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        initView(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        loadListFood();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        new Database(getActivity()).cleanCart();
        for (Order item : cart)
            new Database(getActivity()).addToCart(item);

        //refresh
        loadListFood();
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.rv_cart);

        tvTotalPrice = view.findViewById(R.id.total);
        btnPlace = view.findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0) {

                    showAlertDialog();
                } else {
                    Toast.makeText(getActivity(), "Your Basket is Empty!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadListFood() {
        cart = new Database(getActivity()).getCarts();
        orderList.add(cart);
        adapter = new CartAdapter(cart, getActivity());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        total = 0;
        for (Order order : cart)
            total += (float) (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuanlity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        //
        float tax = (float) (total * 0.06);
        float profit = (float) (total * 0.3);
        total += tax + profit;

        totalPrice = total;

        tvTotalPrice.setText(fmt.format(total));
    }


    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address");

        final EditText edtAddress = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        edtAddress.setLayoutParams(lp);
        alertDialog.setView(edtAddress);
        alertDialog.setIcon(R.drawable.ic_menu_cart);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        tvTotalPrice.getText().toString(),
                        latitude,
                        longitude,
                        cart
                );

                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);

                new Database(getActivity().getBaseContext()).cleanCart();
                Toast.makeText(getActivity(), "Thank you for ordering!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).navigationView.getMenu().getItem(1).setChecked(true);
    }
}
