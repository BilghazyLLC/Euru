$(document).ready(() => {
    var table = $('.table');
    var tableContent = $('#table-content');
    var loading = $('#loading');

    table.hide();
    db.collection('euru_requests').get()
        .then((snapshot) => {
            console.log(snapshot);
            
            if (snapshot && !snapshot.empty) {
                snapshot.forEach(doc => {
                    console.log(doc.data());
                    loading.hide();
                    table.show();

                    var user = doc.data();

                    tableContent.append(`
                        <tr>
                            <td>
                                
                                ${user.category}
                            </td>
                            <td>
                                ${new Date(user.timestamp).toDateString()}
                            </td>
                            <td>
                                ${user.dataKey}
                            </td>
                            <td>
                                ${user.image}
                            </td>
                            <td class="text-center">
                                <a href="#user" class="btn btn-info">View Particulars</a>
                            </td>
                        </tr>
                        `);
                });
            } else {
                table.hide();
                loading.text('Could not find any request...')
                loading.show();
            }
        }).catch((err) => {
            console.log(err.message);
        });
})