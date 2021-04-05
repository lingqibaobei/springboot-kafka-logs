INSERT INTO `uaa`.`sys_role`(`id`, `role_name`) VALUES (1, 'ROLE_SUPER_ADMIN');
INSERT INTO `uaa`.`sys_role`(`id`, `role_name`) VALUES (2, 'ROLE_ADMIN');
INSERT INTO `uaa`.`sys_role`(`id`, `role_name`) VALUES (3, 'ROLE_USER');

-- password=123456,密文
INSERT INTO `uaa`.`sys_user`(`id`, `password`, `username`) VALUES (1, '$2a$10$62ly2.TONU5KKmOY5mQUPeP2tuWjyt0.0SqujX6iWo6tEVMcUesxK', 'dean');


INSERT INTO `uaa`.`sys_user_roles`(`sys_user_id`, `roles_id`) VALUES (1, 2);