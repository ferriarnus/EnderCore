package com.enderio.core.common.util;

import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;

/**
 * An int-int mapping that has a low memory footprint and reasonable lookup times but slow insert times. It also can save and load from/to nbt.
 * <p>
 * Note that keys that have not been stored (or have been deleted) have a value of 0.
 *
 */
public class SparseArray {

  private static final int GROWTH = 10;
  private int[] keys, data;
  private int size;

  public SparseArray() {
    this(null);
  }

  /**
   * Returns tha value that is stored for the given keys or 0 if there's none.
   */
  public int get(int key) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i < 0 || data[i] <= 0) {
      return 0;
    } else {
      return data[i];
    }
  }

  /**
   * Deletes the mapping for the given keys if there is one.
   * <p>
   * Note that the holes that are left will be re-used but will not be purged.
   */
  public void delete(int key) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i >= 0) {
      data[i] = 0;
    }
  }

  /**
   * Adds a new mapping with the given value for the given key.
   */
  public void put(int key, int value) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i < 0) {
      i = ~i;
      if (i >= size) {
        if (size >= keys.length) {
          keys = Arrays.copyOf(keys, size + GROWTH);
          data = Arrays.copyOf(data, size + GROWTH);
        }
        size++;
      } else if (data[i] != 0) {
        int[] newKeys = new int[size + GROWTH];
        System.arraycopy(keys, 0, newKeys, 0, i);
        System.arraycopy(keys, i, newKeys, i + 1, size - i);
        keys = newKeys;
        int[] newData = new int[size + GROWTH];
        System.arraycopy(data, 0, newData, 0, i);
        System.arraycopy(data, i, newData, i + 1, size - i);
        data = newData;
        size++;
      }
    }
    keys[i] = key;
    data[i] = value;
  }

  /**
   * If there are any mappings, stores them in a {@link CompoundNBT} and returns that. If there are no mappings, <code>null</code> is returned.
   */
  @SuppressWarnings("null")
  public CompoundNBT toNBT() {
    if (size > 0) {
      int[] k = new int[size];
      int[] v = new int[size];
      int idx = 0;
      for (int i = 0; i < size; i++) {
        if (data[i] > 0) {
          k[idx] = keys[i];
          v[idx] = data[i];
          idx++;
        }
      }
      if (idx == 0) {
        size = 0;
      } else {
        CompoundNBT result = new CompoundNBT();
        result.putIntArray("k", Arrays.copyOf(k, idx));
        result.putIntArray("v", Arrays.copyOf(v, idx));
        return result;
      }
    }
    return null;
  }

  /**
   * Creates a new object with the mappings stored in the given {@link CompoundNBT}. If the {@link CompoundNBT} is null or doesn't have any mappings, an
   * empty object will be created.
   */
  public SparseArray(CompoundNBT nbt) {
    if (nbt == null || !nbt.contains("k") || !nbt.contains("v")) {
      keys = new int[GROWTH];
      data = new int[GROWTH];
      size = 0;
    } else {
      keys = nbt.getIntArray("k");
      data = nbt.getIntArray("v");
      size = keys.length;
    }
  }

}