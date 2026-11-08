CREATE INDEX idx_click_records_short_code_clicked_at
    ON click_records (short_code, clicked_at);
