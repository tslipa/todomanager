package am.todomanager.recyclers

import android.os.Parcel
import android.os.Parcelable

class ListElement(var id: Int, var title: String?, var description: String?, var date: Long, var time: Long,
                  var icon: Int, var rating: Float, var edited: Long, var notification: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readFloat(),
            parcel.readLong(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeLong(date)
        parcel.writeLong(time)
        parcel.writeInt(icon)
        parcel.writeFloat(rating)
        parcel.writeLong(edited)
        parcel.writeInt(notification)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ListElement> {
        override fun createFromParcel(parcel: Parcel): ListElement {
            return ListElement(parcel)
        }

        override fun newArray(size: Int): Array<ListElement?> {
            return arrayOfNulls(size)
        }
    }
}