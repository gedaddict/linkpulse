CREATE TABLE public.links (
    id bigint NOT NULL,
    created_at timestamp(6) with time zone,
    original_url character varying(255),
    short_code character varying(255),
    short_url character varying(255)
);
