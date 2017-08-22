-- Table: public.model
-- DROP TABLE public.gvi;
-- DROP TABLE public.model;
-- DROP TABLE public.ckc;
-- DROP TABLE public.FreqTrns;

CREATE TABLE public.model
(
  model_id serial NOT NULL SET DEFAULT nextval('m.jc_region_region_id_seq'::regclass),
  model_name text,
  description character varying(255),
  CONSTRAINT model_pkey PRIMARY KEY (model_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.model
  OWNER TO postgres;



CREATE TABLE public.gvi
(
  gvi_id serial NOT NULL,
  kod text,
  name text,
  val bigint,
  unit text,
  type text,
  vw text,
  description text,
  adres bigint,
  wrt bigint,
  min1 bigint,
  max1 bigint,
  def bigint,
  koef bigint,
  size1 bigint,
  model_id bigint,
  CONSTRAINT gvi_pkey PRIMARY KEY (gvi_id))
WITH (
OIDS=FALSE
);
ALTER TABLE public.gvi
OWNER TO postgres;

CREATE TABLE public.ckc
(
  ckc_id serial NOT NULL,
  name text,
  adres bigint,
  val bigint,
  description text,
  model_id bigint,
  CONSTRAINT ckc_pkey PRIMARY KEY (ckc_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.ckc
  OWNER TO postgres;

CREATE TABLE public.FreqTrns
(
  freqtrns_id serial NOT NULL,
  name text,
  adres bigint,
  val bigint,
  description text,
  model_id bigint,
  CONSTRAINT freqtrns_pkey PRIMARY KEY (freqtrns_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.FreqTrns
  OWNER TO postgres;