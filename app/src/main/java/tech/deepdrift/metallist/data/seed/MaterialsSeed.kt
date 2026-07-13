package tech.deepdrift.metallist.data.seed

import tech.deepdrift.metallist.data.db.MaterialEntity

/**
 * Стартовый список материалов с плотностями (г/см³).
 *
 * Значения проверены по нескольким справочным источникам:
 *  - Марочник сталей и сплавов (В.Г. Сорокин)
 *  - ГОСТ 5632, ГОСТ 4784, ГОСТ 613, ГОСТ 15527, ГОСТ 19281
 *  - Справочник «Металлы и сплавы» (tochmeh.ru, e-metall.ru)
 *
 * Для сплавов с широким разбросом плотности приведено среднее по маркам.
 */
object MaterialsSeed {

    val defaults: List<MaterialEntity> = listOf(
        MaterialEntity(name = "Сталь",              densityGCm3 = 7.85, orderIdx = 0),
        MaterialEntity(name = "Алюминий",           densityGCm3 = 2.70, orderIdx = 1),
        MaterialEntity(name = "Бронза",             densityGCm3 = 8.80, orderIdx = 2),
        MaterialEntity(name = "Дюралюминий",        densityGCm3 = 2.78, orderIdx = 3),
        MaterialEntity(name = "Латунь",             densityGCm3 = 8.50, orderIdx = 4),
        MaterialEntity(name = "Магний",             densityGCm3 = 1.74, orderIdx = 5),
        MaterialEntity(name = "Медь",               densityGCm3 = 8.94, orderIdx = 6),
        MaterialEntity(name = "Никель",             densityGCm3 = 8.90, orderIdx = 7),
        MaterialEntity(name = "Нихром",             densityGCm3 = 8.40, orderIdx = 8),
        MaterialEntity(name = "Олово",              densityGCm3 = 7.29, orderIdx = 9),
        MaterialEntity(name = "Свинец",             densityGCm3 = 11.34, orderIdx = 10),
        MaterialEntity(name = "Нержавеющая сталь",  densityGCm3 = 7.90, orderIdx = 11),
        MaterialEntity(name = "Хром",               densityGCm3 = 7.19, orderIdx = 12),
        MaterialEntity(name = "Цинк",               densityGCm3 = 7.13, orderIdx = 13),
        MaterialEntity(name = "Чугун",              densityGCm3 = 7.10, orderIdx = 14),
        MaterialEntity(name = "Титан",              densityGCm3 = 4.51, orderIdx = 15),
        MaterialEntity(name = "Серебро",            densityGCm3 = 10.50, orderIdx = 16),
        MaterialEntity(name = "Золото",             densityGCm3 = 19.32, orderIdx = 17),
        MaterialEntity(name = "Платина",            densityGCm3 = 21.45, orderIdx = 18),
        MaterialEntity(name = "Текстолит",          densityGCm3 = 1.40, orderIdx = 19),
        MaterialEntity(name = "Капролон",           densityGCm3 = 1.15, orderIdx = 20),
        MaterialEntity(name = "Фторопласт-4",       densityGCm3 = 2.20, orderIdx = 21),
        MaterialEntity(name = "Паронит",            densityGCm3 = 1.80, orderIdx = 22),
    )
}
