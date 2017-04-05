var messages = [];

/**
 * Init chat
 * @param {Server} io
 */
exports.init = function(io) {
  io.on('connection', function(socket) {
    socket.on('chat_init', function() {
      socket.emit('chat_messages', messages);
    });
    socket.on('send_message', function(message) {
      if (message.length <= 0) return;
      messages.push(message);
      io.emit('new_messages', messages);
    });
  });
};