package gjj_unit_test.gjj_buletooth_demo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 作者：gjj on 2016/3/9 11:17
 * 邮箱：Gujj512@163.com
 */
public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private Context context;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        this.context=context;
    }

    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {

            view = View.inflate(context,R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
        }else {
            viewHolder.deviceName.setText(R.string.unknown_device);
        }
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
