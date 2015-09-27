DROP TABLE IF EXISTS "images"."metadata";
CREATE TABLE "images"."metadata" (
	"uuid" varchar NOT NULL COLLATE "default",
	"low_res_hist" varchar NOT NULL COLLATE "default",
	"high_res_hist" varchar COLLATE "default",
	"timestamp" int8 NOT NULL
)
WITH (OIDS=FALSE);


-- ----------------------------
--  Primary key structure for table metadata
-- ----------------------------
ALTER TABLE "images"."metadata" ADD PRIMARY KEY ("uuid") NOT DEFERRABLE INITIALLY IMMEDIATE;

-- ----------------------------
--  Indexes structure for table metadata
-- ----------------------------
CREATE INDEX  "high_res_index" ON "images"."metadata" USING btree(high_res_hist COLLATE "default" ASC NULLS LAST);
CREATE INDEX  "low_res_index" ON "images"."metadata" USING btree(low_res_hist COLLATE "default" ASC NULLS LAST);
CREATE INDEX  "timestamp_index" ON "images"."metadata" USING btree("timestamp" ASC NULLS LAST);
CREATE INDEX  "uuid_index" ON "images"."metadata" USING btree(uuid COLLATE "default" ASC NULLS LAST);