-- Estado de la sesion de entrenamiento: se inicia al crearla y se finaliza explicitamente.
-- Una sesion finalizada ya no admite cambios en sus series.
ALTER TABLE sesion_entrenamiento ADD COLUMN iniciada_en TIMESTAMP WITH TIME ZONE;
UPDATE sesion_entrenamiento SET iniciada_en = creado_en;
ALTER TABLE sesion_entrenamiento ALTER COLUMN iniciada_en SET NOT NULL;

ALTER TABLE sesion_entrenamiento ADD COLUMN finalizada_en TIMESTAMP WITH TIME ZONE;
ALTER TABLE sesion_entrenamiento
    ADD CONSTRAINT ck_sesion_finalizada CHECK (finalizada_en IS NULL OR finalizada_en >= iniciada_en);
