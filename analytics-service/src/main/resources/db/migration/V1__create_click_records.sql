CREATE TABLE public.click_records (
    id bigint NOT NULL,
    clicked_at timestamp(6) with time zone,
    referrer character varying(255),
    short_code character varying(255),
    user_agent character varying(255)
);
