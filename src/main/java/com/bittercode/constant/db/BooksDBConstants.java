package com.bittercode.constant.db;

public interface BooksDBConstants {
	
    public static String TABLE_BOOK = "books";
	
	public static String COLUMN_NAME = "name";
	public static String COLUMN_BARCODE = "barcode";
	public static String COLUMN_AUTHOR = "author";
	public static String COLUMN_PRICE = "price";
	public static String COLUMN_QUANTITY = "quantity";

	// --- Cột bổ sung cho UC03 (Chi tiết sách) ---
	public static String COLUMN_PUBLISHER = "publisher";
	public static String COLUMN_IMAGE_URL = "image_url";
	public static String COLUMN_DESCRIPTION = "description";
	public static String COLUMN_CATEGORY_ID = "category_id";
	public static String COLUMN_STATUS = "status";
}
