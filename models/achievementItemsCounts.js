var bookshelf = require('../config/bookshelf');

var AchievementItemsCounts = bookshelf.Model.extend({
  tableName: 'achievements_items_counts'
});

module.exports = AchievementItemsCounts;