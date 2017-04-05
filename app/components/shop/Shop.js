import React from 'react';
import { connect } from 'react-redux';
import DocumentTitle from 'react-document-title';
import ReactSpinner from 'react-spinjs';
import $ from 'jquery';
import notify from './../../actions/notify';
import Cart from './Cart';
import ShopItem from './ShopItem';
import 'whatwg-fetch';
import 'jquery.formstyler';

class Shop extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      items: [],
      count: 0,
      page: 1,
      search: '',
      filters: {
        rarity: '',
        quality: '',
        type: '',
        special: ''
      },
      sort: 0,
      loaded: false
    };
  }

  componentDidMount() {
    const self = this;
    this.loadItems();
    $('.select select').styler({
      selectSmartPositioning: false
    })
    .change(() => self.handleFilter(this.name, this.value));
  }

  componentDidUpdate() {
    const height = (document.getElementsByClassName('itemShop')[0].clientHeight + 140) + 'px';
    document.getElementsByClassName('b-basket')[0].style.height = height;
    document.getElementsByClassName('b-basket-item')[0].style.height = height;
  }

  loadItems() {
    const self = this;
    this.setState({ loaded: false });
    fetch('/get_shop_items', {
      method: 'post',
      headers: {'Content-Type': 'application/json'},
      credentials: 'same-origin',
      body: JSON.stringify({
        game: this.props.gameName
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_ITEMS_ERROR'));
        if (data.success) {
          let dataItems = data.items, items = {};
          for (let itemId in dataItems) {
            let item = dataItems[itemId];
            item.shop = item.ids;
            item.cart = [];
            item.isShow = false;
            items['k' + itemId] = item;
          }
          self.setState({ items }, () => {
            self.filterItems(() => self.sortItems(0));
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_ITEMS_ERROR'));
      console.log(err);
    });
  }

  handleShowCart() {
    this.refs.cart.handleToggle();
  }

  filterItems(callback) {
    this.setState({ loaded: false });
    let { items, search, filters } = this.state;
    const regexp = new RegExp(search, 'i');
    let count = 0;
    for (let itemId in items) {
      if (!items.hasOwnProperty(itemId)) continue;
      const item = items[itemId];
      if (
        regexp.test(item.name) &&
        (filters.rarity === '' || item.rarity === filters.rarity) &&
        (filters.quality === '' || item.quality === filters.quality) &&
        (filters.type === '' || item.type === filters.type) &&
        (filters.special === '' || item.special === filters.special)
      ) {
        count++;
        items[itemId].isShow = true;
      } else items[itemId].isShow = false;
    }
    this.setState({
      items: items,
      count: count,
      page: 1,
      loaded: true
    }, callback);
  }

  sortItems(type) {
    this.setState({ loaded: false });
    const sortPrice = (asc) => (_a, _b) => {
      const a = _a.value.price, b = _b.value.price,
        d = asc ? a - b : b - a;
      return d === 0 ?
        compareSplitters(
          new Splitter(_a.value.name),
          new Splitter(_b.value.name)
        ) : d;
    };
    let { items } = this.state;
    let itemsArray = [];
    for (let itemId in items) {
      if (!items.hasOwnProperty(itemId)) continue;
      itemsArray.push({
        key: itemId,
        value: items[itemId]
      });
    }
    if (type === 0) itemsArray.sort(sortPrice(false));
    else if (type === 1) itemsArray.sort(sortPrice(true));
    else if (type === 2) itemsArray = naturalSort(itemsArray, true, item => item.value.name);
    else if (type === 3) itemsArray = naturalSort(itemsArray, false, item => item.value.name);
    let newItems = {};
    for (let itemKV of itemsArray) {
      newItems[itemKV.key] = itemKV.value;
    }
    this.setState({
      items: newItems,
      sort: type,
      loaded: true
    });
  }

  handlePage(page) {
    return (e) => {
      if (typeof page === 'undefined') {
        const value = e.target.value;
        if (value === '') return this.setState({ page: '' });
        page = Number(value);
      } else e.preventDefault();
      const maxPage = Math.ceil(this.state.count/24) || 1;
      if (isNaN(page) || page < 1) page = 1;
      else if (page > maxPage) page = maxPage;
      this.setState({ page });
    };
  }

  handleSearch(e) {
    this.setState({ search: e.target.value }, this.filterItems);
  }

  handleFilter(name, value) {
    let { filters } = this.state;
    filters[name] = value;
    this.setState({ filters }, this.filterItems);
  }

  handleSortItems(type) {
    return (e) => {
      e.preventDefault();
      const {sort} = this.state;
      this.sortItems(
        type === 0 ? sort === 0 ? 1 : 0 :
        type === 1 ? sort === 2 ? 3 : 2 : 0
      );
    };
  }

  handleClickItem(toCart) {
    return (itemId) => {
      let { items, count } = this.state;
      if (items.hasOwnProperty(itemId)) {
        if (toCart) items[itemId].cart.push(items[itemId].shop.splice(0, 1));
        else items[itemId].shop.push(items[itemId].cart.splice(0, 1));
        this.setState({ items, count }, () => this.props.dispatch(notify('ADD_CART')));
      }
    };
  }

  handleBuy(e) {
    e.preventDefault();
    const self = this;
    const { items } = this.state;
    let _items = [];
    for (let itemId in items) {
      if (!items.hasOwnProperty(itemId)) continue;
      const item = items[itemId];
      if (item.cart.length > 0) {
        _items = _items.concat(item.cart);
      }
    }
    fetch('/buy_items', {
      method: 'post',
      headers: {'Content-Type': 'application/json'},
      credentials: 'same-origin',
      body: JSON.stringify({
        items: _items.join(',')
      })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('BUY_ERROR'));
        if (data.success) self.loadItems();
      });
    })
    .catch(err => {
      self.props.dispatch(notify('BUY_ERROR'));
      console.log(err);
    });
  }

  render() {
    const { gameName } = this.props;
    const { items, count, page, search, sort, loaded } = this.state;
    let shopItems = [], cartItems = [], cartPrice = 0;
    if (count > 0 && page !== '') {
      let i = -1;
      for (let itemId in items) {
        if (!items.hasOwnProperty(itemId)) continue;
        const item = items[itemId];
        if (item.isShow && item.shop.length > 0) { i++;
          if (i < (page-1)*24) continue;
          else {
            shopItems.push(
              <ShopItem
                key={itemId}
                id={itemId}
                name={item.name}
                icon={item.icon}
                color={item.color}
                price={item.price}
                discount={item.discount}
                count={item.shop.length}
                handleClick={this.handleClickItem(true).bind(this)}
              />
            );
          }
        }
        if (i === page*24-1) break;
      }
    }
    for (let itemId in items) {
      if (!items.hasOwnProperty(itemId)) continue;
      const item = items[itemId];
      if (item.cart.length > 0) {
        cartItems.push(
          <ShopItem
            key={itemId}
            id={itemId}
            name={item.name}
            icon={item.icon}
            color={item.color}
            price={item.price}
            discount={item.discount}
            count={item.cart.length}
            handleClick={this.handleClickItem(false).bind(this)}
          />
        );
        cartPrice += item.price;
      }
    }
    let filters = {
      rarity: [],
      quality: [],
      type: [],
      special: []
    };
    if (gameName === 'csgo') filters = filtersCsgo;
    else if (gameName === 'dota') filters = filtersDota;
    return (
      <DocumentTitle title="Магазин">
        <div className="blockGame">
          <div className="select">
            <div className="contentP">
              <div className="header">
                <h1>Магазин</h1>
              </div>
              <div className="selectItem">
                <div className="block">
                  <span>Редкость:</span>
                  <select name="rarity">
                    <option value="">Выберите...</option>
                    {filters.rarity.map(item => <option key={item.value} value={item.value}>{item.name}</option>)}
                  </select>
                </div>
                <div className="block">
                  <span>Качество:</span>
                  <select name="quality">
                    <option value="">Выберите...</option>
                    {filters.quality.map(item => <option key={item.value} value={item.value}>{item.name}</option>)}
                  </select>
                </div>
                <div className="block">
                  <span>Тип предмета:</span>
                  <select name="type">
                    <option value="">Выберите...</option>
                    {filters.type.map(item => <option key={item.value} value={item.value}>{item.name}</option>)}
                  </select>
                </div>
                <div className="block">
                  <span>{gameName !== 'dota' ? 'Категория' : 'Герой'}:</span>
                  <select name="special">
                    <option value="">Выберите...</option>
                    {filters.special.map(item => <option key={item.value} value={item.value}>{item.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="selectPage">
                <span className="sell">
                  <span>Сортировать по:</span>
                  <a className={sort === 0 ? 'active down' : sort === 1 ? 'active up' : null} href="#" onClick={this.handleSortItems(0).bind(this)}>цене</a>
                  <a className={sort === 2 ? 'active down' : sort === 3 ? 'active up' : null} href="#" onClick={this.handleSortItems(1).bind(this)}>названию</a>
                </span>
                <span className="search">
                  <input type="text" name="search" placeholder="Поиск предметов" value={search} onChange={this.handleSearch.bind(this)} />
                  <button><i className="fa fa-search"/></button>
                </span>
              </div>
            </div>
          </div>
          <div className="itemShop">
            <i className="b-basket-toggle fa fa-shopping-basket" onClick={this.handleShowCart.bind(this)}>&nbsp;<span>Корзина</span></i>
            <div className="contentP">
              {loaded ? count > 0 ? shopItems :
                <span className="b-not-found">Нет предметов</span> : <ReactSpinner color="#a0a0a0"/>
              }
            </div>
          </div>
          <Cart
            ref="cart"
            listItems={cartItems}
            price={cartPrice}
            handleBuy={this.handleBuy.bind(this)}
          />
          <div className="itemNav">
            <div className="nav">
              <a href="#" onClick={this.handlePage(page-1).bind(this)}><i className="fa fa-angle-double-left"/></a>
              <input type="text" name="page" value={page} onChange={this.handlePage().bind(this)} />
              <span>из {Math.ceil(count/24) || 1}</span>
              <a href="#" onClick={this.handlePage(page+1).bind(this)}><i className="fa fa-angle-double-right"/></a>
            </div>
          </div>
        </div>
      </DocumentTitle>
    );
  }
}

export default connect()(Shop);





function naturalSort(array, asc, extractor) {
  const splitters = array.map(item => new Splitter(item, extractor)),
    sorted = splitters.sort(asc ? compareSplitters : (a, b) => compareSplitters(b, a));
  return sorted.map(splitter => splitter.item);
}

class Splitter {
  constructor(item, extractor) {
    this.index = 0;
    this.from = 0;
    this.parts = [];
    this.completed = false;
    this.item = item;
    this.key = (typeof extractor === 'function') ? extractor(item) : item;
  }
  count() {
    return this.parts.length;
  }
  part(i) {
    while (this.parts <= i && !this.completed) this.next();
    return (i < this.parts.length) ? this.parts[i] : null;
  }
  next() {
    if (this.index < this.key.length) {
      while (++this.index) {
        const currentIsDigit = isDigit(this.key.charAt(this.index - 1)),
          nextChar = this.key.charAt(this.index),
          currentIsLast = (this.index === this.key.length),
          isBorder = currentIsLast || xor(currentIsDigit, isDigit(nextChar));
        if (isBorder) {
          const partStr = this.key.slice(this.from, this.index);
          this.parts.push(new Part(partStr, currentIsDigit));
          this.from = this.index;
          break;
        }
      }
    } else this.completed = true;
  }
}

class Part {
  constructor(text, isNumber) {
    this.isNumber = isNumber;
    this.value = isNumber ? Number(text) : text;
  }
}

function compareSplitters(sp1, sp2) {
  const compare = (a, b) => (a < b) ? -1 : (a > b) ? 1 : 0;
  let i = 0;
  do {
    const first = sp1.part(i), second = sp2.part(i);
    if (first !== null && second !== null) {
      if (xor(first.isNumber, second.isNumber)) {
        return first.isNumber ? -1 : 1;
      } else {
        const comp = compare(first.value, second.value);
        if (comp !== 0) return comp;
      }
    } else return compare(sp1.count(), sp2.count());
  } while (++i);
}

function xor(a, b) {
  return a ? !b : b;
}

function isDigit(chr) {
  const charCode = (ch) => ch.charCodeAt(0), code = charCode(chr);
  return (code >= charCode('0')) && (code <= charCode('9'));
}



const filtersCsgo = {
  rarity: [
    { name: 'Ширпотреб', value: 'Rarity_Common_Weapon' },
    { name: 'Промышленное качество', value: 'Rarity_Uncommon_Weapon' },
    { name: 'Армейское качество', value: 'Rarity_Rare_Weapon' },
    { name: 'Запрещенное', value: 'Rarity_Mythical_Weapon' },
    { name: 'Засекреченное', value: 'Rarity_Legendary_Weapon' },
    { name: 'Тайное', value: 'Rarity_Ancient_Weapon' },
    { name: 'Базового класса', value: 'Rarity_Common' },
    { name: 'Высшего класса', value: 'Rarity_Rare' },
    { name: 'Примечательного типа', value: 'Rarity_Mythical' },
    { name: 'Экзотичного вида', value: 'Rarity_Legendary' },
    { name: 'Экстраординарного типа', value: 'Rarity_Ancient' },
    { name: 'Контрабанда', value: 'Rarity_Contraband' }
  ],
  quality: [
    { name: 'Прямо с завода', value: 'WearCategory0' },
    { name: 'Немного поношенное', value: 'WearCategory1' },
    { name: 'После полевых испытаний', value: 'WearCategory2' },
    { name: 'Поношенное', value: 'WearCategory3' },
    { name: 'Закаленное в боях', value: 'WearCategory4' },
    { name: 'Не покрашено', value: 'WearCategoryNA' }
  ],
  type: [
    { name: 'Пистолет', value: 'CSGO_Type_Pistol' },
    { name: 'Пистолет-пулемёт', value: 'CSGO_Type_SMG' },
    { name: 'Винтовка', value: 'CSGO_Type_Rifle' },
    { name: 'Снайперская винтовка', value: 'CSGO_Type_SniperRifle' },
    { name: 'Дробовик', value: 'CSGO_Type_Shotgun' },
    { name: 'Пулемёт', value: 'CSGO_Type_Machinegun' },
    { name: 'Контейнер', value: 'CSGO_Type_WeaponCase' },
    { name: 'Нож', value: 'CSGO_Type_Knife' },
    { name: 'Наклейка', value: 'CSGO_Tool_Sticker' },
    { name: 'Граффити', value: 'CSGO_Type_Spray' },
    { name: 'Перчатки', value: 'Type_Hands' },
    { name: 'Набор музыки', value: 'CSGO_Type_MusicKit' },
    { name: 'Коллекционный', value: 'CSGO_Type_Collectible' },
    { name: 'Ключ', value: 'CSGO_Tool_WeaponCase_KeyTag' },
    { name: 'Пропуск', value: 'CSGO_Type_Ticket' },
    { name: 'Подарок', value: 'CSGO_Tool_GiftTag' },
    { name: 'Инструмент', value: 'CSGO_Type_Tool' },
    { name: 'Ярлык', value: 'CSGO_Tool_Name_TagTag' }
  ],
  special: [
    { name: 'Обычный', value: 'normal' },
    { name: 'StatTrak™', value: 'strange' },
    { name: 'Сувенирный', value: 'tournament' },
    { name: '★', value: 'unusual' },
    { name: '★ StatTrak™', value: 'unusual_strange' }
  ]
};

const filtersDota = {
  rarity: [
    { name: 'Common', value: 'Rarity_Common' },
    { name: 'Uncommon', value: 'Rarity_Uncommon' },
    { name: 'Rare', value: 'Rarity_Rare' },
    { name: 'Mythical', value: 'Rarity_Mythical' },
    { name: 'Immortal', value: 'Rarity_Immortal' },
    { name: 'Legendary', value: 'Rarity_Legendary' },
    { name: 'Arcana', value: 'Rarity_Arcana' },
    { name: 'Ancient', value: 'Rarity_Ancient' }
  ],
  quality: [
    { name: 'Standard', value: 'unique' },
    { name: 'Inscribed', value: 'strange' },
    { name: 'Auspicious', value: 'lucky' },
    { name: 'Heroic', value: 'tournament' },
    { name: 'Genuine', value: 'genuine' },
    { name: 'Frozen', value: 'frozen' },
    { name: 'Cursed', value: 'haunted' },
    { name: 'Autographed', value: 'autographed' },
    { name: 'Base', value: 'base' },
    { name: 'Corrupted', value: 'corrupted' },
    { name: 'Unusual', value: 'unusual' },
    { name: 'Infused', value: 'infused' },
    { name: 'Exalted', value: 'exalted' },
    { name: 'Elder', value: 'vintage' },
    { name: 'Base', value: 'normal' },
    { name: 'Legacy', value: 'legacy' }
  ],
  type: [
    { name: 'Билет', value: 'league' },
    { name: 'Вымпел', value: 'pennant' },
    { name: 'Загрузочный экран', value: 'loading_screen' },
    { name: 'Инструмент', value: 'tool' },
    { name: 'Ключ от сокровищницы', value: 'key' },
    { name: 'Комментатор', value: 'announcer' },
    { name: 'Курьер', value: 'courier' },
    { name: 'Ландшафт', value: 'terrain' },
    { name: 'Музыка', value: 'music' },
    { name: 'Набор', value: 'bundle' },
    { name: 'Набор курсоров', value: 'cursor_pack' },
    { name: 'Насмешка', value: 'taunt' },
    { name: 'Разное', value: 'misc' },
    { name: 'Рецепт', value: 'dynamic_recipe' },
    { name: 'Самоцвет/руна', value: 'socket_gem' },
    { name: 'Смайлик', value: 'emoticon_tool' },
    { name: 'Сокровищница', value: 'treasure_chest' },
    { name: 'Стиль интерфейса', value: 'hud_skin' },
    { name: 'Украшение', value: 'wearable' },
    { name: 'Устаревшая сокровищница', value: 'retired_treasure_chest' },
    { name: 'Ward', value: 'ward' }
  ],
  special: [{"name":"Другие","value":"DOTA_OtherType"},{"name":"Abaddon","value":"npc_dota_hero_abaddon"},{"name":"Alchemist","value":"npc_dota_hero_alchemist"},{"name":"Ancient Apparition","value":"npc_dota_hero_ancient_apparition"},{"name":"Anti-Mage","value":"npc_dota_hero_antimage"},{"name":"Axe","value":"npc_dota_hero_axe"},{"name":"Bane","value":"npc_dota_hero_bane"},{"name":"Batrider","value":"npc_dota_hero_batrider"},{"name":"Beastmaster","value":"npc_dota_hero_beastmaster"},{"name":"Bloodseeker","value":"npc_dota_hero_bloodseeker"},{"name":"Bounty Hunter","value":"npc_dota_hero_bounty_hunter"},{"name":"Brewmaster","value":"npc_dota_hero_brewmaster"},{"name":"Bristleback","value":"npc_dota_hero_bristleback"},{"name":"Broodmother","value":"npc_dota_hero_broodmother"},{"name":"Centaur Warrunner","value":"npc_dota_hero_centaur"},{"name":"Chaos Knight","value":"npc_dota_hero_chaos_knight"},{"name":"Chen","value":"npc_dota_hero_chen"},{"name":"Clinkz","value":"npc_dota_hero_clinkz"},{"name":"Clockwerk","value":"npc_dota_hero_rattletrap"},{"name":"Crystal Maiden","value":"npc_dota_hero_crystal_maiden"},{"name":"Dark Seer","value":"npc_dota_hero_dark_seer"},{"name":"Dazzle","value":"npc_dota_hero_dazzle"},{"name":"Death Prophet","value":"npc_dota_hero_death_prophet"},{"name":"Disruptor","value":"npc_dota_hero_disruptor"},{"name":"Doom","value":"npc_dota_hero_doom_bringer"},{"name":"Dragon Knight","value":"npc_dota_hero_dragon_knight"},{"name":"Drow Ranger","value":"npc_dota_hero_drow_ranger"},{"name":"Earth Spirit","value":"npc_dota_hero_earth_spirit"},{"name":"Earthshaker","value":"npc_dota_hero_earthshaker"},{"name":"Elder Titan","value":"npc_dota_hero_elder_titan"},{"name":"Ember Spirit","value":"npc_dota_hero_ember_spirit"},{"name":"Enchantress","value":"npc_dota_hero_enchantress"},{"name":"Enigma","value":"npc_dota_hero_enigma"},{"name":"Faceless Void","value":"npc_dota_hero_faceless_void"},{"name":"Gyrocopter","value":"npc_dota_hero_gyrocopter"},{"name":"Huskar","value":"npc_dota_hero_huskar"},{"name":"Invoker","value":"npc_dota_hero_invoker"},{"name":"Juggernaut","value":"npc_dota_hero_juggernaut"},{"name":"Keeper of the Light","value":"npc_dota_hero_keeper_of_the_light"},{"name":"Kunkka","value":"npc_dota_hero_kunkka"},{"name":"Legion Commander","value":"npc_dota_hero_legion_commander"},{"name":"Leshrac","value":"npc_dota_hero_leshrac"},{"name":"Lich","value":"npc_dota_hero_lich"},{"name":"Lifestealer","value":"npc_dota_hero_life_stealer"},{"name":"Lina","value":"npc_dota_hero_lina"},{"name":"Lion","value":"npc_dota_hero_lion"},{"name":"Lone Druid","value":"npc_dota_hero_lone_druid"},{"name":"Luna","value":"npc_dota_hero_luna"},{"name":"Lycan","value":"npc_dota_hero_lycan"},{"name":"Magnus","value":"npc_dota_hero_magnataur"},{"name":"Medusa","value":"npc_dota_hero_medusa"},{"name":"Meepo","value":"npc_dota_hero_meepo"},{"name":"Mirana","value":"npc_dota_hero_mirana"},{"name":"Monkey King","value":"npc_dota_hero_monkey_king"},{"name":"Morphling","value":"npc_dota_hero_morphling"},{"name":"Naga Siren","value":"npc_dota_hero_naga_siren"},{"name":"Nature's Prophet","value":"npc_dota_hero_furion"},{"name":"Necrophos","value":"npc_dota_hero_necrolyte"},{"name":"Night Stalker","value":"npc_dota_hero_night_stalker"},{"name":"Nyx Assassin","value":"npc_dota_hero_nyx_assassin"},{"name":"Ogre Magi","value":"npc_dota_hero_ogre_magi"},{"name":"Omniknight","value":"npc_dota_hero_omniknight"},{"name":"Outworld Devourer","value":"npc_dota_hero_obsidian_destroyer"},{"name":"Phantom Assassin","value":"npc_dota_hero_phantom_assassin"},{"name":"Phantom Lancer","value":"npc_dota_hero_phantom_lancer"},{"name":"Phoenix","value":"npc_dota_hero_phoenix"},{"name":"Puck","value":"npc_dota_hero_puck"},{"name":"Pudge","value":"npc_dota_hero_pudge"},{"name":"Pugna","value":"npc_dota_hero_pugna"},{"name":"Queen of Pain","value":"npc_dota_hero_queenofpain"},{"name":"Razor","value":"npc_dota_hero_razor"},{"name":"Riki","value":"npc_dota_hero_riki"},{"name":"Rubick","value":"npc_dota_hero_rubick"},{"name":"Sand King","value":"npc_dota_hero_sand_king"},{"name":"Shadow Demon","value":"npc_dota_hero_shadow_demon"},{"name":"Shadow Fiend","value":"npc_dota_hero_nevermore"},{"name":"Shadow Shaman","value":"npc_dota_hero_shadow_shaman"},{"name":"Silencer","value":"npc_dota_hero_silencer"},{"name":"Skywrath Mage","value":"npc_dota_hero_skywrath_mage"},{"name":"Slardar","value":"npc_dota_hero_slardar"},{"name":"Slark","value":"npc_dota_hero_slark"},{"name":"Sniper","value":"npc_dota_hero_sniper"},{"name":"Spectre","value":"npc_dota_hero_spectre"},{"name":"Spirit Breaker","value":"npc_dota_hero_spirit_breaker"},{"name":"Storm Spirit","value":"npc_dota_hero_storm_spirit"},{"name":"Sven","value":"npc_dota_hero_sven"},{"name":"Techies","value":"npc_dota_hero_techies"},{"name":"Templar Assassin","value":"npc_dota_hero_templar_assassin"},{"name":"Terrorblade","value":"npc_dota_hero_terrorblade"},{"name":"Tidehunter","value":"npc_dota_hero_tidehunter"},{"name":"Timbersaw","value":"npc_dota_hero_shredder"},{"name":"Tinker","value":"npc_dota_hero_tinker"},{"name":"Tiny","value":"npc_dota_hero_tiny"},{"name":"Treant Protector","value":"npc_dota_hero_treant"},{"name":"Troll Warlord","value":"npc_dota_hero_troll_warlord"},{"name":"Tusk","value":"npc_dota_hero_tusk"},{"name":"Undying","value":"npc_dota_hero_undying"},{"name":"Ursa","value":"npc_dota_hero_ursa"},{"name":"Vengeful Spirit","value":"npc_dota_hero_vengefulspirit"},{"name":"Venomancer","value":"npc_dota_hero_venomancer"},{"name":"Visage","value":"npc_dota_hero_visage"},{"name":"Warlock","value":"npc_dota_hero_warlock"},{"name":"Weaver","value":"npc_dota_hero_weaver"},{"name":"Windranger","value":"npc_dota_hero_windrunner"},{"name":"Winter Wyvern","value":"npc_dota_hero_winter_wyvern"},{"name":"Witch Doctor","value":"npc_dota_hero_witch_doctor"},{"name":"Wraith King","value":"npc_dota_hero_skeleton_king"},{"name":"Zeus","value":"npc_dota_hero_zuus"}]
};