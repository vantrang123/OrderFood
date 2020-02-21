package com.trangdv.orderfood.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.OrderStatusAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Request;

import java.util.ArrayList;
import java.util.List;

public class OrderStatusFragment extends Fragment implements OrderStatusAdapter.ItemListener {
    FirebaseDatabase database;
    DatabaseReference requests;
    OrderStatusAdapter orderStatusAdapter;
    public RecyclerView rvListOrder;
    public RecyclerView.LayoutManager layoutManager;

    List<String> listIds = new ArrayList<>();
    List<Request> listRequests = new ArrayList<>();
    String requestId;

    public static OrderStatusFragment newInstance(String phone) {
        OrderStatusFragment orderStatus = new OrderStatusFragment();
        Bundle bundle = new Bundle();
        bundle.putString("FoodId", phone);
        orderStatus.setArguments(bundle);
        return orderStatus;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orderstatus, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Order Status");
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        rvListOrder = view.findViewById(R.id.listOrders);
        initView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);
        loadOrders(Common.currentUser.getUserPhone());
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);
    }

    private void loadOrders(String phone) {
        requests.orderByChild("phone").equalTo(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    requestId = dsp.getKey();
                    listIds.add(requestId);
                    Request request = dsp.getValue(Request.class);
                    listRequests.add(request);
                }
                viewData(listRequests, listIds);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void viewData(List<Request> listRequests, List<String> listIds) {
        orderStatusAdapter = new OrderStatusAdapter(getContext(), listRequests, listIds, this);
        rvListOrder.setAdapter(orderStatusAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        listIds.clear();
        listRequests.clear();
    }

    @Override
    public void dispatchToOrderDetail(int position) {

    }
}
