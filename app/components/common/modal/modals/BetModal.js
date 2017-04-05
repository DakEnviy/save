import React from 'react';
import Modal from '../Modal';

class BetModal extends React.Component {
  render() {
    return (
      <Modal
        title={this.props.title}
        width={this.props.width}
        head={<div className="gradient"></div>}
        hideModal={this.props.hideModal}
        showOk={false}
        footer={
          <div>
            <button onClick={this.props.newBetModal} className="modal-ok">Предметы</button>
            <button onClick={this.props.newCardsBetModal} className="modal-ok">Карточки</button>
          </div>
        }
      ><span/></Modal>
    );
  }
}

export default BetModal;