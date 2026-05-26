-- =============================================================================
--  password_reset.sql  -  Proyecto Desarrollo Alumni
--
--  Los hashes de credenciales del script original (desarrollo_alumni.sql) son
--  bcrypt SIMULADOS y no corresponden a ninguna contrasena real, por lo que
--  ningun usuario semilla podria iniciar sesion.
--
--  Este script sustituye esos hashes por hashes bcrypt VALIDOS. Tras ejecutarlo,
--  TODOS los usuarios semilla tienen la contrasena:   Loyola2026!
--
--  Ejecucion (despues de desarrollo_alumni.sql):
--    - phpMyAdmin: selecciona la BD 'desarrollo_alumni' -> pestana SQL -> pega y ejecuta
--    - linea de comandos:  mysql -u root desarrollo_alumni < password_reset.sql
-- =============================================================================
USE desarrollo_alumni;

UPDATE credenciales SET hash_contrasena = '$2a$10$Bdyw2Z6zYI21hlFq11ilcuNfKN88h1wusnlB83qbJYF4vyoQXHQQW' WHERE id_usuario = 1;
UPDATE credenciales SET hash_contrasena = '$2a$10$jEsdaA/NqqOJEb8nKUscWOZJ/eX9KBYPc.1TDcNUuBSqPV5IFl0Eu' WHERE id_usuario = 2;
UPDATE credenciales SET hash_contrasena = '$2a$10$Jl0UWbqt6PDiK9ecYFlHTOdIHO6OoH4x5HDr9HEKX21xR.IilOC0e' WHERE id_usuario = 3;
UPDATE credenciales SET hash_contrasena = '$2a$10$OyXI8yYNVvhg9jt7KGoXJ.qawSBI89KyKksZhpgRHupQvgUSz.V5W' WHERE id_usuario = 4;
UPDATE credenciales SET hash_contrasena = '$2a$10$0FNqvilsgt1w1aRs7UkZ2e.yfJsLcWplUjjedaavo2ZvUhoJ/yBNS' WHERE id_usuario = 5;
UPDATE credenciales SET hash_contrasena = '$2a$10$DEMnd/priH8/c.6n8grPTudRcPaWZjBD.e0ry79n9.nXLnyvaKZci' WHERE id_usuario = 6;
UPDATE credenciales SET hash_contrasena = '$2a$10$TuyHUmsd300PP/AXhqGDEemHuc2u82U/CpkusWvmDe68FbztU4wXS' WHERE id_usuario = 7;
UPDATE credenciales SET hash_contrasena = '$2a$10$f7svHVuyghkrBx6fEItUle8NAy6V4126bargundpcxKFR.U/rOVQ.' WHERE id_usuario = 8;
UPDATE credenciales SET hash_contrasena = '$2a$10$oVGbqGySd17IW3cQ.RKRA.fF/LQ6aAkxwUBi3az1s9mRWgulg7mxC' WHERE id_usuario = 9;
UPDATE credenciales SET hash_contrasena = '$2a$10$mzk.faGXVOo9o/f9XdcvYe8rX4Vn3K6hGVO51/ffqGUTgqFiuZt0S' WHERE id_usuario = 10;
UPDATE credenciales SET hash_contrasena = '$2a$10$avOE7KHnARNNme7IBqIij.YAZcaKyYt9de2JRjrjCeyNhGzjq2dyS' WHERE id_usuario = 11;
UPDATE credenciales SET hash_contrasena = '$2a$10$x4yj/SEMSDeNt1DKyHRx5.4B/T/spi7dptYbVPA6Jlc5dKXINv83m' WHERE id_usuario = 12;
UPDATE credenciales SET hash_contrasena = '$2a$10$uajygs760Dq98WlVrzIXLOgALj2FMeSPUWlEDh/eBlSFpzIkr6QQa' WHERE id_usuario = 13;
UPDATE credenciales SET hash_contrasena = '$2a$10$NkBaWIcgyGGJCkpt0ekLI.5i4uFId0WskRO1ksZWe/UHqzV5FR6c6' WHERE id_usuario = 14;
UPDATE credenciales SET hash_contrasena = '$2a$10$qbXe1cpMgFXDPuY4mnteTeuRye1V4Ta30mmu5m.maKwqGpgHDe78i' WHERE id_usuario = 15;
UPDATE credenciales SET hash_contrasena = '$2a$10$aTYjr2hqALo5i4b7jvPAhu//oMJNsMOX2oVhRhQipniWxJM05e06q' WHERE id_usuario = 16;
UPDATE credenciales SET hash_contrasena = '$2a$10$yxhArSSvSvmC/D.o8KBA1uuSoT7cYFUeAZWJDCRyF.kPGHGjRKPji' WHERE id_usuario = 17;
UPDATE credenciales SET hash_contrasena = '$2a$10$/ashPul2RKLIWdnzGEAIA.vDiTaIhTwA.TvOeLhXvSk/XxxIuQVmC' WHERE id_usuario = 18;
UPDATE credenciales SET hash_contrasena = '$2a$10$UdQ/o4nWpeOXIrYuHTJ6o.sdbvEw9NBQqQVTK2mb351oEuK.Ml/KK' WHERE id_usuario = 19;
UPDATE credenciales SET hash_contrasena = '$2a$10$LVH9tpR/tsahwFdbutmmlODf37y7fd.XkvdetFIUHbREDNB1PXlu.' WHERE id_usuario = 20;
UPDATE credenciales SET hash_contrasena = '$2a$10$FSaGL4Z9oOOBYVV9NkZZh.XAN7ELVKVQY8ma8IItSpiyD8ZKtSfPq' WHERE id_usuario = 21;
UPDATE credenciales SET hash_contrasena = '$2a$10$XuRfQlAxoDXb1RCW8iwgU.RJeZ2JzQsS4rKuBZ2vZ5i5fbHsv993m' WHERE id_usuario = 22;
UPDATE credenciales SET hash_contrasena = '$2a$10$yCaptUqgKFaN/48gMLlfA.yy34jYrH.CCHtgZIQrRCfiH7/O.gQyC' WHERE id_usuario = 23;
UPDATE credenciales SET hash_contrasena = '$2a$10$2ZtUkHLmUWUwj922CkWxUOrS/Z35n.wFYlbexoSIH.DABWss55mqS' WHERE id_usuario = 24;
UPDATE credenciales SET hash_contrasena = '$2a$10$Ji9L3piouOLPVwdIMLbW8eFmCAMJZDpIF7OnJSuSdBPdpnowyMzry' WHERE id_usuario = 25;
UPDATE credenciales SET hash_contrasena = '$2a$10$hA/RIlxa2/3F2id.LSgNQeHjxEIWkuopE3iu9y8F437/VMtv0e6Me' WHERE id_usuario = 26;
UPDATE credenciales SET hash_contrasena = '$2a$10$r0imdAvcVEOHBlO00aybPuDel8aaM5teOJaNeXCmfYiF6aM6WrSMa' WHERE id_usuario = 27;
UPDATE credenciales SET hash_contrasena = '$2a$10$9UNJgA0i7fSkh5/53QG87OPPmkao.hQwbHuaKj35e9g0nuBRlzZ9y' WHERE id_usuario = 28;
UPDATE credenciales SET hash_contrasena = '$2a$10$iD8.gC18UN/dY0CGc/iaMuOkA/ar8macAayk.gqJZ5hhaSMYuRD/y' WHERE id_usuario = 29;
UPDATE credenciales SET hash_contrasena = '$2a$10$pwqtUdPhHE1vDmbPzeJy5em1DzCPRghSDioZXgfrzV3vocwaNltkW' WHERE id_usuario = 30;
UPDATE credenciales SET hash_contrasena = '$2a$10$MhGfZ4s1Tm8zqYjc3Hb.iOSyEoy0zcCHvLGHgPCsqa6rJ0xzaA4kO' WHERE id_usuario = 31;
UPDATE credenciales SET hash_contrasena = '$2a$10$V4sq4cikEbS./z8rXFc5WuqmeXDauJdW7KwcCe.N5O088qoo9Asvy' WHERE id_usuario = 32;

-- Reactiva la cuenta pendiente de prueba (id 14) para poder usarla directamente.
-- Comenta esta linea si quieres conservar el escenario 'PENDIENTE_ACTIVACION'.
UPDATE usuario SET estado = 'ACTIVO' WHERE id_usuario = 14 AND estado = 'PENDIENTE_ACTIVACION';

-- Verificacion
SELECT id_usuario, usuario_login, LEFT(hash_contrasena,7) AS formato_hash FROM credenciales ORDER BY id_usuario;
