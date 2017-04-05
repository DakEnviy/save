import React from 'react';
import ReactSpinner from 'react-spinjs';
import { connect } from 'react-redux';
import notify from './../../../../actions/notify';
import Modal from '../Modal';
import Item from '../../../game/Item';
import 'whatwg-fetch';

class ItemsModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loaded: false,
      items: [],
      selected: []
    };
  }

  componentDidMount() {
    const self = this;
    fetch('/get_items', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({ game: this.props.gameName })
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_ITEMS_ERROR'));
        if (data.success) {
          self.setState({
            loaded: true,
            items: data.items
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_ITEMS_ERROR'));
      console.log(err);
    });
  }

  handleSelect(itemId, isAdd) {
    if (isAdd) {
      this.setState({
        selected: this.state.selected.concat([itemId])
      });
    } else {
      let selected = this.state.selected.slice();
      let i = selected.indexOf(itemId);
      if (i !== -1) selected.splice(i, 1);
      this.setState({
        selected: selected
      });
    }
  }

  handleOk() {
    this.props.handleOk(this.props.gameName, this.state.selected);
  }

  handleDeposit(e) {
    e.preventDefault();
    if (this.props.user.id) window.open('/deposit', e.target.href);
    else this.props.dispatch(notify('INVALID_USER'));
  }

  render() {
    const { items } = this.state;
    let listItems = [];
    for (let i = 0, l = items.length; i < l; i++) {
      const item = items[i];
      listItems.push(<Item
        key={item.item_id}
        itemId={item.item_id}
        name={item.name}
        icon={item.icon}
        color={item.color}
        price={item.price}
        isSelect={true}
        handleSelect={this.handleSelect.bind(this)}
      />);
    }
    return (
      <Modal
        title={this.props.title}
        width={this.props.width}
        head={<div className="gradient"></div>}
        hideModal={this.props.hideModal}
        okText={this.props.okText}
        onOk={this.handleOk.bind(this)}
        footer={<a href="/deposit" className="modal-ok" onClick={this.handleDeposit.bind(this)}>Пополнить инвентарь</a>}
      >
        <div className="new-bet">
          {this.state.loaded ?
            items.length > 0 ?
              listItems :
              <span style={{
                display: 'block',
                padding: '60px 0px 30px',
                textAlign: 'center',
                color: '#f8a200'
              }}>Нет предметов</span> :
            <ReactSpinner color="#a0a0a0" position="relative" />
          }
        </div>
      </Modal>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    user: state.user
  };
};

export default connect(mapStateToProps)(ItemsModal);