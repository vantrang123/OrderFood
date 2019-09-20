package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.listener.ItemClickListener;
import com.trangdv.orderfood.model.Category;
import com.trangdv.orderfood.viewholder.MenuViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
public class HomeFragment extends Fragment {

    FirebaseDatabase database;
    DatabaseReference category;
    RecyclerView recycler_menu;

    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recycler_menu = view.findViewById(R.id.rv_menu);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Categories");

        //load menu
        layoutManager = new LinearLayoutManager(getActivity());
        recycler_menu.setLayoutManager(layoutManager);
        fetchData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("HomeFragment", "onDetach: " );
    }

    public void fetchData() {
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, category) {
            @Override
            protected void populateViewHolder(MenuViewHolder menuViewHolder, final Category category, final int i) {
                menuViewHolder.txtMenuName.setText(category.getName());
                Picasso.with(getContext())
                        .load(category.getImage())
                        .into(menuViewHolder.imgMenu);

                final Category clickItem = category;
                menuViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent intent = new Intent(getActivity(), FoodActivity.class);
                        intent.putExtra("CategoryId", adapter.getRef(i).getKey());
                        startActivity(intent);

                    }
                });
            }
        };
        recycler_menu.setAdapter(adapter);
//        recycler_menu.getAdapter().notifyDataSetChanged();
        Log.e("HomeFragment", "fetchData: "+adapter.getItemCount() );
    }

}
